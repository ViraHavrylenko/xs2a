/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ais.AccountAdditionalInformationAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class Xs2aAisConsentMapper {
    // TODO remove this dependency. Should not be dependencies between spi-api and consent-api https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/437
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final Xs2aToSpiAccountAccessMapper xs2aToSpiAccountAccessMapper;
    private final ConsentDataMapper consentDataMapper;
    private final RequestProviderService requestProviderService;

    public CreateAisConsentRequest mapToCreateAisConsentRequest(CreateConsentReq req, PsuIdData psuData, TppInfo tppInfo, int allowedFrequencyPerDay, String internalRequestId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest aisRequest = new CreateAisConsentRequest();
                       aisRequest.setPsuData(psuData);
                       aisRequest.setTppInfo(tppInfo);
                       aisRequest.setRequestedFrequencyPerDay(r.getFrequencyPerDay());
                       aisRequest.setAllowedFrequencyPerDay(allowedFrequencyPerDay);
                       aisRequest.setAccess(mapToAisAccountAccessInfo(req.getAccess()));
                       aisRequest.setValidUntil(r.getValidUntil());
                       aisRequest.setRecurringIndicator(r.isRecurringIndicator());
                       aisRequest.setCombinedServiceIndicator(r.isCombinedServiceIndicator());
                       aisRequest.setTppRedirectUri(r.getTppRedirectUri());
                       aisRequest.setInternalRequestId(internalRequestId);
                       aisRequest.setTppNotificationUri(Optional.ofNullable(req.getTppNotificationData()).map(TppNotificationData::getTppNotificationUri).orElse(null));
                       aisRequest.setNotificationSupportedModes(Optional.ofNullable(req.getTppNotificationData()).map(TppNotificationData::getNotificationModes).orElse(null));
                       return aisRequest;
                   })
                   .orElse(null);
    }

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent aisConsent) {
        return Optional.ofNullable(aisConsent)
                   .map(ac -> new SpiAccountConsent(
                            ac.getId(),
                            xs2aToSpiAccountAccessMapper.mapToAccountAccess(ac.getAccess()),
                            ac.isRecurringIndicator(),
                            ac.getValidUntil(),
                            ac.getExpireDate(),
                            ac.getFrequencyPerDay(),
                            ac.getLastActionDate(),
                            ac.getConsentStatus(),
                            ac.isWithBalance(),
                            ac.getConsentTppInformation().isTppRedirectPreferred(),
                            xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(ac.getPsuIdDataList()),
                            ac.getTppInfo(),
                            ac.getAisConsentRequestType(),
                            ac.getStatusChangeTimestamp(),
                            ac.getCreationTimestamp()
                        )
                   )
                   .orElse(null);
    }

    public UpdateConsentPsuDataReq mapToUpdateConsentPsuDataReq(UpdateAuthorisationRequest request,
                                                                AuthorisationProcessorResponse response) {
        return Optional.ofNullable(response)
                   .map(data -> {
                       UpdateConsentPsuDataReq req = new UpdateConsentPsuDataReq();
                       req.setPsuData(response.getPsuData());
                       req.setConsentId(request.getBusinessObjectId());
                       req.setAuthorizationId(request.getAuthorisationId());
                       req.setAuthenticationMethodId(Optional.ofNullable(data.getChosenScaMethod())
                                                         .map(AuthenticationObject::getAuthenticationMethodId)
                                                         .orElse(null));
                       req.setScaAuthenticationData(request.getScaAuthenticationData());
                       req.setScaStatus(data.getScaStatus());
                       req.setAuthorisationType(AuthorisationType.AIS);
                       return req;
                   })
                   .orElse(null);
    }

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateAuthorisationRequest request, PsuIdData psuData) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getBusinessObjectId());
        accountConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    public AisAccountAccessInfo mapToAisAccountAccessInfo(AccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(mapToListAccountInfo(access.getAccounts()));
        accessInfo.setBalances(mapToListAccountInfo(access.getBalances()));
        accessInfo.setTransactions(mapToListAccountInfo(access.getTransactions()));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                            .orElse(null));

        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                  .orElse(null));

        accessInfo.setAvailableAccountsWithBalance(Optional.ofNullable(access.getAvailableAccountsWithBalance())
                                                       .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                                       .orElse(null));

        accessInfo.setAccountAdditionalInformationAccess(Optional.ofNullable(access.getAdditionalInformationAccess())
                                                             .map(this::mapToAccountAdditionalInformationAccess)
                                                             .orElse(null));

        return accessInfo;
    }

    public CmsConsent mapToCmsConsent(CreateConsentReq request, PsuIdData psuData, TppInfo tppInfo, int allowedFrequencyPerDay) {
        CmsConsent cmsConsent = new CmsConsent();

        AisConsentData aisConsentData = new AisConsentData(request.getAccess(), AccountAccess.EMPTY_ACCESS, request.isCombinedServiceIndicator());
        byte[] aisConsentDataBytes = consentDataMapper.getBytesFromAisConsentData(aisConsentData);
        cmsConsent.setConsentData(aisConsentDataBytes);

        ConsentTppInformation tppInformation = new ConsentTppInformation();
        tppInformation.setTppInfo(tppInfo);
        tppInformation.setTppFrequencyPerDay(request.getFrequencyPerDay());
        tppInformation.setTppNotificationUri(Optional.ofNullable(request.getTppNotificationData()).map(TppNotificationData::getTppNotificationUri).orElse(null));
        tppInformation.setTppNotificationSupportedModes(Optional.ofNullable(request.getTppNotificationData()).map(TppNotificationData::getNotificationModes).orElse(null));
        cmsConsent.setTppInformation(tppInformation);

        AuthorisationTemplate authorisationTemplate = new AuthorisationTemplate();
        authorisationTemplate.setTppRedirectUri(request.getTppRedirectUri());
        cmsConsent.setAuthorisationTemplate(authorisationTemplate);

        cmsConsent.setFrequencyPerDay(allowedFrequencyPerDay);
        cmsConsent.setInternalRequestId(requestProviderService.getInternalRequestIdString());
        cmsConsent.setValidUntil(request.getValidUntil());
        cmsConsent.setRecurringIndicator(request.isRecurringIndicator());
        cmsConsent.setPsuIdDataList(Collections.singletonList(psuData));
        cmsConsent.setConsentType(ConsentType.AIS);

        return cmsConsent;
    }

    private AccountAdditionalInformationAccess mapToAccountAdditionalInformationAccess(AdditionalInformationAccess info) {
        return new AccountAdditionalInformationAccess(mapToListAccountInfoOrDefault(info.getOwnerName(), null));
    }

    private List<AccountInfo> mapToListAccountInfo(List<AccountReference> refs) {
        return emptyIfNull(refs).stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private List<AccountInfo> mapToListAccountInfoOrDefault(List<AccountReference> refs, List<AccountInfo> defaultValue) {
        return Optional.ofNullable(refs)
                   .map(this::mapToListAccountInfo)
                   .orElse(defaultValue);
    }

    private AccountInfo mapToAccountInfo(AccountReference ref) {
        AccountReferenceSelector selector = ref.getUsedAccountReferenceSelector();
        return AccountInfo.builder()
                   .resourceId(ref.getResourceId())
                   .accountIdentifier(selector.getAccountValue())
                   .currency(Optional.ofNullable(ref.getCurrency())
                                 .map(Currency::getCurrencyCode)
                                 .orElse(null))
                   .accountReferenceType(selector.getAccountReferenceType())
                   .aspspAccountId(ref.getAspspAccountId())
                   .build();
    }

    public AisConsent mapToAisConsent(CmsConsent ais) {
        return Optional.ofNullable(ais)
                   .map(ac -> {

                       AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(ac.getConsentData());
                       AisConsentData consentData = new AisConsentData(aisConsentData.getTppAccountAccess(), aisConsentData.getAspspAccountAccess(), aisConsentData.isCombinedServiceIndicator());

                       AisConsent aisConsent = new AisConsent();
                       aisConsent.setId(ac.getId());
                       aisConsent.setConsentData(consentData);
                       aisConsent.setRecurringIndicator(ac.isRecurringIndicator());
                       aisConsent.setValidUntil(ac.getValidUntil());
                       aisConsent.setExpireDate(ac.getExpireDate());
                       aisConsent.setFrequencyPerDay(ac.getFrequencyPerDay());
                       aisConsent.setLastActionDate(ac.getLastActionDate());
                       aisConsent.setConsentStatus(ac.getConsentStatus());
                       aisConsent.setAuthorisationTemplate(ac.getAuthorisationTemplate());
                       aisConsent.setPsuIdDataList(ac.getPsuIdDataList());
                       aisConsent.setConsentTppInformation(ac.getTppInformation());
                       aisConsent.setMultilevelScaRequired(ac.isMultilevelScaRequired());
                       aisConsent.setAuthorisations(mapToAccountConsentAuthorisation(ac.getAuthorisations()));
                       aisConsent.setStatusChangeTimestamp(ac.getStatusChangeTimestamp());
                       aisConsent.setUsages(ac.getUsages());
                       aisConsent.setCreationTimestamp(ac.getCreationTimestamp());

                       return aisConsent;
                   })
                   .orElse(null);
    }

    private List<AccountConsentAuthorization> mapToAccountConsentAuthorisation(List<Authorisation> authorisations) {
        if (CollectionUtils.isEmpty(authorisations)) {
            return Collections.emptyList();
        }
        return authorisations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private AccountConsentAuthorization mapToAccountConsentAuthorisation(Authorisation authorisation) {
        return Optional.ofNullable(authorisation)
                   .map(auth -> {
                       AccountConsentAuthorization accountConsentAuthorisation = new AccountConsentAuthorization();
                       accountConsentAuthorisation.setId(auth.getAuthorisationId());
                       accountConsentAuthorisation.setPsuIdData(auth.getPsuIdData());
                       accountConsentAuthorisation.setScaStatus(auth.getScaStatus());
                       return accountConsentAuthorisation;
                   })
                   .orElse(null);
    }
}
