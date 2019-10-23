/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service("AIS_SCAMETHODSELECTED")
public class AisScaAuthenticatedStage extends AisScaStage<UpdateConsentPsuDataReq, AccountConsentAuthorization, UpdateConsentPsuDataResponse> {

    private final SpiContextDataProvider spiContextDataProvider;
    private final RequestProviderService requestProviderService;

    public AisScaAuthenticatedStage(Xs2aAisConsentService aisConsentService,
                                    SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                    RequestProviderService requestProviderService,
                                    AisConsentSpi aisConsentSpi,
                                    Xs2aAisConsentMapper aisConsentMapper,
                                    Xs2aToSpiPsuDataMapper psuDataMapper,
                                    SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                    SpiContextDataProvider spiContextDataProvider,
                                    SpiErrorMapper spiErrorMapper) {
        super(aisConsentService, aspspConsentDataProviderFactory, aisConsentSpi, aisConsentMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.requestProviderService = requestProviderService;
    }

    /**
     * Verifying authorisation code workflow: verifying code from the request
     * (returns response with error code in case of wrong code being provided), updates consent status
     * and returns response with FINALISED status.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request, AccountConsentAuthorization authorisationResponse) {
        String consentId = request.getConsentId();
        String authorisationId = request.getAuthorisationId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}]. AIS_SCAMETHODSELECTED stage. Apply authorisation when update consent PSU data has failed. Consent not found by id. Error msg: {}.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, errorHolder);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }
        AccountConsent accountConsent = accountConsentOptional.get();

        PsuIdData psuData = extractPsuIdData(request, authorisationResponse);

        SpiResponse<SpiVerifyScaAuthorisationResponse> spiResponse = aisConsentSpi.verifyScaAuthorisation(spiContextDataProvider.provideWithPsuIdData(psuData), aisConsentMapper.mapToSpiScaConfirmation(request, psuData), aisConsentMapper.mapToSpiAccountConsent(accountConsent), aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_SCAMETHODSELECTED stage. Verify SCA authorisation failed when update PSU data. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, request.getAuthorizationId(), psuData.getPsuId(), errorHolder);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        ConsentStatus responseConsentStatus = spiResponse.getPayload().getConsentStatus();

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !accountConsent.isMultilevelScaRequired()) {
            aisConsentService.updateMultilevelScaRequired(consentId, true);
        }

        if (accountConsent.getConsentStatus() != responseConsentStatus) {
            aisConsentService.updateConsentStatus(consentId, responseConsentStatus);
        }
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);

        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, request.getAuthorisationId());
    }
}
