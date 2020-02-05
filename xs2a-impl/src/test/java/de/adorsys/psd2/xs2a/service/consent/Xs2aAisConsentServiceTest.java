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


package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.*;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aAisConsentServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final String AUTHENTICATION_METHOD_ID = "19ff-4b5a-8188";
    private static final String TPP_ID = "Test TppId";
    private static final String REQUEST_URI = "request/uri";
    private static final String REDIRECT_URI = "request/redirect_uri";
    private static final String NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.DECOUPLED;
    private static final CreateConsentReq CREATE_CONSENT_REQ = buildCreateConsentReq();
    private static final CreateConsentRequest CREATE_AIS_CONSENT_REQUEST = new CreateConsentRequest();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final CmsAccountConsent AIS_ACCOUNT_CONSENT = new CmsAccountConsent();
    private static final de.adorsys.psd2.xs2a.domain.consent.AccountConsent ACCOUNT_CONSENT = createConsent(CONSENT_ID);
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final AisConsentAuthorizationRequest AIS_CONSENT_AUTHORIZATION_REQUEST = buildAisConsentAuthorizationRequest();
    private static final AisConsentAuthorizationResponse AIS_CONSENT_AUTHORIZATION_RESPONSE = new AisConsentAuthorizationResponse();
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = new AccountConsentAuthorization();
    private static final List<String> STRING_LIST = Collections.singletonList(AUTHORISATION_ID);
    private static final List<AuthenticationObject> AUTHENTICATION_OBJECT_LIST = Collections.singletonList(new AuthenticationObject());
    private static final List<CmsScaMethod> CMS_SCA_METHOD_LIST = Collections.singletonList(new CmsScaMethod(AUTHORISATION_ID, true));
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private Xs2aAisConsentService xs2aAisConsentService;
    @Mock
    private ConsentServiceEncrypted consentServiceEncrypted;
    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @Mock
    private AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private Xs2aAisConsentAuthorisationMapper aisConsentAuthorisationMapper;
    @Mock
    private FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    @Mock
    private Xs2aAuthenticationObjectToCmsScaMethodMapper xs2AAuthenticationObjectToCmsScaMethodMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private LoggingContextService loggingContextService;

    @Test
    void createConsent_success() throws WrongChecksumException {
        //Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1, INTERNAL_REQUEST_ID))
            .thenReturn(CREATE_AIS_CONSENT_REQUEST);
        when(consentServiceEncrypted.createConsent(CREATE_AIS_CONSENT_REQUEST))
            .thenReturn(CmsResponse.<CreateConsentResponse>builder().payload(new CreateConsentResponse(CONSENT_ID, AIS_ACCOUNT_CONSENT, null)).build());
        when(aisConsentMapper.mapToAccountConsent(AIS_ACCOUNT_CONSENT))
            .thenReturn(ACCOUNT_CONSENT);

        Xs2aCreateAisConsentResponse expected = new Xs2aCreateAisConsentResponse(CONSENT_ID, ACCOUNT_CONSENT, null);

        //When
        Optional<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        //Then
        assertTrue(actualResponse.isPresent());
        assertEquals(expected, actualResponse.get());
    }

    @Test
    void createConsent_failed() throws WrongChecksumException {
        //Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1, INTERNAL_REQUEST_ID))
            .thenReturn(CREATE_AIS_CONSENT_REQUEST);
        when(consentServiceEncrypted.createConsent(CREATE_AIS_CONSENT_REQUEST))
            .thenReturn(CmsResponse.<CreateConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<Xs2aCreateAisConsentResponse> actualResponse = xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);

        //Then
        assertFalse(actualResponse.isPresent());
    }

    @Test
    void getAccountConsentById_success() {
        //Given
        when(consentServiceEncrypted.getAccountConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsAccountConsent>builder().payload(AIS_ACCOUNT_CONSENT).build());
        when(aisConsentMapper.mapToAccountConsent(AIS_ACCOUNT_CONSENT))
            .thenReturn(ACCOUNT_CONSENT);

        //When
        Optional<de.adorsys.psd2.xs2a.domain.consent.AccountConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(ACCOUNT_CONSENT);
    }

    @Test
    void getAccountConsentById_failed() {
        //Given
        when(consentServiceEncrypted.getAccountConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsAccountConsent>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<de.adorsys.psd2.xs2a.domain.consent.AccountConsent> actualResponse = xs2aAisConsentService.getAccountConsentById(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success() {
        //Given
        when(consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_false() {
        //Given
        when(consentServiceEncrypted.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2aAisConsentService.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void createAisConsentAuthorization_success() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(aisConsentAuthorisationMapper.mapToAisConsentAuthorization(SCA_STATUS, PSU_DATA, SCA_APPROACH, REDIRECT_URI, NOK_REDIRECT_URI))
            .thenReturn(AIS_CONSENT_AUTHORIZATION_REQUEST);
        when(aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(CONSENT_ID, AIS_CONSENT_AUTHORIZATION_REQUEST))
            .thenReturn(CmsResponse.<CreateAisConsentAuthorizationResponse>builder().payload(buildCreateAisConsentAuthorizationResponse()).build());
        when(requestProviderService.getTppRedirectURI())
            .thenReturn(REDIRECT_URI);
        when(requestProviderService.getTppNokRedirectURI())
            .thenReturn(NOK_REDIRECT_URI);

        //When
        Optional<CreateAisConsentAuthorizationResponse> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(buildCreateAisConsentAuthorizationResponse());
    }

    @Test
    void createAisConsentAuthorization_false() {
        //Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);
        when(aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(CONSENT_ID), any()))
            .thenReturn(CmsResponse.<CreateAisConsentAuthorizationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<CreateAisConsentAuthorizationResponse> actualResponse = xs2aAisConsentService.createAisConsentAuthorization(CONSENT_ID, SCA_STATUS, PSU_DATA);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder().payload(AIS_CONSENT_AUTHORIZATION_RESPONSE).build());
        when(aisConsentAuthorisationMapper.mapToAccountConsentAuthorization(AIS_CONSENT_AUTHORIZATION_RESPONSE))
            .thenReturn(ACCOUNT_CONSENT_AUTHORIZATION);

        //When
        Optional<AccountConsentAuthorization> actualResponse = xs2aAisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(ACCOUNT_CONSENT_AUTHORIZATION);
    }

    @Test
    void getAccountConsentAuthorizationById_failed() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<AccountConsentAuthorization> actualResponse = xs2aAisConsentService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationSubResources_success() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.getAuthorisationsByConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<List<String>>builder().payload(STRING_LIST).build());

        //When
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(STRING_LIST);
    }

    @Test
    void getAuthorisationSubResources_failed() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.getAuthorisationsByConsentId(CONSENT_ID))
            .thenReturn(CmsResponse.<List<String>>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<List<String>> actualResponse = xs2aAisConsentService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void getAuthorisationScaStatus_success() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        //When
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isTrue();
        assertThat(actualResponse.get()).isEqualTo(SCA_STATUS);
    }

    @Test
    void getAuthorisationScaStatus_failed() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        Optional<ScaStatus> actualResponse = xs2aAisConsentService.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    void isAuthenticationMethodDecoupled_success() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void isAuthenticationMethodDecoupled_failed() {
        //Given
        when(aisConsentAuthorisationServiceEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2aAisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void saveAuthenticationMethods_success() {
        //Given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(aisConsentAuthorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void saveAuthenticationMethods_failed() {
        //Given
        when(xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(AUTHENTICATION_OBJECT_LIST))
            .thenReturn(CMS_SCA_METHOD_LIST);
        when(aisConsentAuthorisationServiceEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, CMS_SCA_METHOD_LIST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2aAisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, AUTHENTICATION_OBJECT_LIST);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void updateConsentStatus_shouldStoreConsentStatusInLoggingContext() throws WrongChecksumException {
        // Given
        when(consentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(consentServiceEncrypted).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService).storeConsentStatus(CONSENT_STATUS);
    }

    @Test
    void updateConsentStatus_failure_shouldNotStoreConsentStatusInLoggingContext() throws WrongChecksumException {
        // Given
        when(consentServiceEncrypted.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        // When
        xs2aAisConsentService.updateConsentStatus(CONSENT_ID, CONSENT_STATUS);

        // Then
        verify(consentServiceEncrypted).updateConsentStatusById(CONSENT_ID, CONSENT_STATUS);
        verify(loggingContextService, never()).storeConsentStatus(any());
    }

    @Test
    void consentActionLog() throws WrongChecksumException {
        //Given
        ActionStatus actionStatus = ActionStatus.SUCCESS;
        ArgumentCaptor<AisConsentActionRequest> argumentCaptor = ArgumentCaptor.forClass(AisConsentActionRequest.class);
        //When
        xs2aAisConsentService.consentActionLog(TPP_ID, CONSENT_ID, actionStatus, REQUEST_URI, true, null, null);
        //Then
        verify(aisConsentServiceEncrypted).checkConsentAndSaveActionLog(argumentCaptor.capture());

        AisConsentActionRequest aisConsentActionRequest = argumentCaptor.getValue();
        assertThat(aisConsentActionRequest.getTppId()).isEqualTo(TPP_ID);
        assertThat(aisConsentActionRequest.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(aisConsentActionRequest.getActionStatus()).isEqualTo(actionStatus);
        assertThat(aisConsentActionRequest.getRequestUri()).isEqualTo(REQUEST_URI);
        assertThat(aisConsentActionRequest.isUpdateUsage()).isTrue();
    }

    @Test
    void createConsentCheckInternalRequestId() throws WrongChecksumException {
        //Given
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);
        ArgumentCaptor<CreateConsentRequest> argumentCaptor = ArgumentCaptor.forClass(CreateConsentRequest.class);
        when(frequencyPerDateCalculationService.getMinFrequencyPerDay(CREATE_CONSENT_REQ.getFrequencyPerDay()))
            .thenReturn(1);
        CreateConsentRequest createAisConsentRequesWithInternalRequestId = new CreateConsentRequest();
        createAisConsentRequesWithInternalRequestId.setInternalRequestId(INTERNAL_REQUEST_ID);
        when(aisConsentMapper.mapToCreateAisConsentRequest(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO, 1, INTERNAL_REQUEST_ID))
            .thenReturn(createAisConsentRequesWithInternalRequestId);
        when(consentServiceEncrypted.createConsent(any()))
            .thenReturn(CmsResponse.<CreateConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        xs2aAisConsentService.createConsent(CREATE_CONSENT_REQ, PSU_DATA, TPP_INFO);
        verify(consentServiceEncrypted).createConsent(argumentCaptor.capture());

        //Then
        CreateConsentRequest createAisConsentRequest = argumentCaptor.getValue();
        assertThat(createAisConsentRequest.getInternalRequestId()).isEqualTo(INTERNAL_REQUEST_ID);

    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static CreateConsentReq buildCreateConsentReq() {
        CreateConsentReq createConsentReq = new CreateConsentReq();
        createConsentReq.setFrequencyPerDay(1);
        return createConsentReq;
    }

    private static AisConsentAuthorizationRequest buildAisConsentAuthorizationRequest() {
        AisConsentAuthorizationRequest consentAuthorization = new AisConsentAuthorizationRequest();
        consentAuthorization.setPsuData(PSU_DATA);
        consentAuthorization.setScaStatus(SCA_STATUS);
        consentAuthorization.setScaApproach(SCA_APPROACH);
        return consentAuthorization;
    }

    private static de.adorsys.psd2.xs2a.domain.consent.AccountConsent createConsent(String id) {
        return new de.adorsys.psd2.xs2a.domain.consent.AccountConsent(id, new Xs2aAccountAccess(null, null, null, null, null, null, null), new Xs2aAccountAccess(null, null, null, null, null, null, null), false, LocalDate.now(), LocalDate.now(), 4, LocalDate.now(), ConsentStatus.VALID, false, false, null, null, AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap(), OffsetDateTime.now());
    }

    private static CreateAisConsentAuthorizationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAisConsentAuthorizationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null);
    }
}
