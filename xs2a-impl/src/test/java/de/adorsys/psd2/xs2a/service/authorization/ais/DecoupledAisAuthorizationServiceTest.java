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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DecoupledAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final Authorisation ACCOUNT_CONSENT_AUTHORIZATION = buildAccountConsentAuthorization();
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final CreateConsentAuthorizationResponse CREATE_CONSENT_AUTHORIZATION_RESPONSE = buildCreateConsentAuthorizationResponse();

    @InjectMocks
    private DecoupledAisAuthorizationService decoupledAisAuthorizationService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private AisScaStageAuthorisationFactory scaStageAuthorisationFactory;

    @Test
    void createConsentAuthorization_success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(buildConsent(CONSENT_ID)));
        when(aisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA))
            .thenReturn(Optional.of(buildCreateAuthorisationResponse()));

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(CREATE_CONSENT_AUTHORIZATION_RESPONSE);
    }

    @Test
    void createConsentAuthorization_wrongConsentId_fail() {
        // Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<CreateConsentAuthorizationResponse> actualResponse = decoupledAisAuthorizationService.createConsentAuthorization(PSU_DATA, WRONG_CONSENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void updateConsentPsuData() {
        UpdateConsentPsuDataReq authorisationRequest = buildUpdateConsentPsuDataReq();
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();

        UpdateConsentPsuDataReq mappedUpdatePsuDataRequest = new UpdateConsentPsuDataReq();
        when(aisConsentMapper.mapToUpdateConsentPsuDataReq(authorisationRequest, processorResponse))
            .thenReturn(mappedUpdatePsuDataRequest);

        AuthorisationProcessorResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(authorisationRequest, processorResponse);

        assertEquals(processorResponse, actualResponse);
        verify(aisConsentService).updateConsentAuthorization(mappedUpdatePsuDataRequest);
    }

    @Test
    void updateConsentPsuData_errorResponse() {
        UpdateConsentPsuDataReq authorisationRequest = buildUpdateConsentPsuDataReq();
        AuthorisationProcessorResponse processorResponse = buildAuthorisationProcessorResponseWithError();

        AuthorisationProcessorResponse actualResponse = decoupledAisAuthorizationService.updateConsentPsuData(authorisationRequest, processorResponse);

        assertEquals(processorResponse, actualResponse);
        verify(aisConsentService, never()).updateConsentAuthorization(any());
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(ACCOUNT_CONSENT_AUTHORIZATION));

        // When
        Optional<Authorisation> actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    void getAccountConsentAuthorizationById_wrongIds_fail() {
        // Given
        when(aisConsentService.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<Authorisation> actualResponse = decoupledAisAuthorizationService.getAccountConsentAuthorizationById(WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_wrongIds_fail() {
        // Given
        when(aisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actualResponse = decoupledAisAuthorizationService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getScaApproachServiceType_success() {
        // When
        ScaApproach actualResponse = decoupledAisAuthorizationService.getScaApproachServiceType();

        // Then
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    private static CreateConsentAuthorizationResponse buildCreateConsentAuthorizationResponse() {
        CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();
        resp.setConsentId(CONSENT_ID);
        resp.setAuthorisationId(AUTHORISATION_ID);
        resp.setScaStatus(ScaStatus.RECEIVED);
        resp.setPsuIdData(PSU_DATA);
        return resp;
    }

    private static Authorisation buildAccountConsentAuthorization() {
        Authorisation authorisation = new Authorisation();
        authorisation.setScaStatus(ScaStatus.RECEIVED);
        return authorisation;
    }

    private static AisConsent buildConsent(String id) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(id);
        return aisConsent;
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq authorisationRequest = new UpdateConsentPsuDataReq();
        authorisationRequest.setPsuData(PSU_DATA);
        return authorisationRequest;
    }

    private CreateAuthorisationResponse buildCreateAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", PSU_DATA);
    }

    private AuthorisationProcessorResponse buildAuthorisationProcessorResponseWithError() {
        AuthorisationProcessorResponse processorResponse = new AuthorisationProcessorResponse();
        processorResponse.setErrorHolder(ErrorHolder.builder(ErrorType.AIS_400).build());
        return processorResponse;
    }
}
