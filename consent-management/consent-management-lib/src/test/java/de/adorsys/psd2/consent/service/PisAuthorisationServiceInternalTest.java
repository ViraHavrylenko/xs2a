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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisAuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.IteratorUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.PATC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationServiceInternalTest {
    @InjectMocks
    private PisAuthorisationServiceInternal pisAuthorisationServiceInternal;
    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PisAuthorisationRepository pisAuthorisationRepository;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Spy
    private PsuDataMapper psuDataMapper;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    @Mock
    private CmsPsuService cmsPsuService;

    private PisCommonPaymentData pisCommonPaymentData;
    private List<PisAuthorization> pisAuthorizationList = new ArrayList<>();
    private PisAuthorization pisAuthorization;

    private PisPaymentData pisPaymentData;
    private final long PIS_PAYMENT_DATA_ID = 1;
    private static final String EXTERNAL_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String PAYMENT_ID_WRONG = "5bbdcb28ca10e8e14a41b12f";
    private static final String PAYMENT_ID_WRONG_TRANSACTION_STATUS = "6bbdcb28ca10e8e14a41b12f";
    private static final String FINALISED_AUTHORISATION_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_CANCELLATION_AUTHORISATION_ID = "2a112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("id", "type", "corporate ID", "corporate type");
    private final static PsuData PSU_DATA = new PsuData("id", "type", "corporate ID", "corporate type");
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PSU_ID_DATA, ScaApproach.REDIRECT, TPP_REDIRECT_URIs);
    private static final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        pisAuthorization = buildPisAuthorisation(EXTERNAL_ID, PaymentAuthorisationType.CREATED);
        pisCommonPaymentData = buildPisCommonPaymentData();
        pisPaymentData = buildPaymentData(pisCommonPaymentData);
        pisAuthorizationList.add(buildPisAuthorisation(EXTERNAL_ID, PaymentAuthorisationType.CANCELLED));
        pisAuthorizationList.add(buildPisAuthorisation(AUTHORISATION_ID, PaymentAuthorisationType.CREATED));
    }

    @Test
    void getAuthorisationScaStatus_success() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, PaymentAuthorisationType.CREATED)).thenReturn(Optional.of(pisAuthorization));

        // When
        Optional<ScaStatus> actual = pisAuthorisationServiceInternal.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongPaymentId() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, PaymentAuthorisationType.CREATED)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisAuthorisationServiceInternal.getAuthorisationScaStatus(PAYMENT_ID_WRONG, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongAuthorisationId() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(WRONG_AUTHORISATION_ID, PaymentAuthorisationType.CREATED)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisAuthorisationServiceInternal.getAuthorisationScaStatus(PAYMENT_ID, WRONG_AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getAuthorisationByPaymentIdSuccess() {
        //When
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisPaymentData.getPaymentData())).thenReturn(pisPaymentData.getPaymentData());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisAuthorisationServiceInternal.getAuthorisationsByPaymentId(PAYMENT_ID, PaymentAuthorisationType.CANCELLED);
        //Assert
        assertTrue(authorizationByPaymentId.isPresent());
        assertEquals(1, authorizationByPaymentId.get().size());
        assertEquals(pisAuthorizationList.get(0).getExternalId(), authorizationByPaymentId.get().get(0));
    }

    @Test
    void getAuthorisationByPaymentIdWrongPaymentId() {
        //When
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID_WRONG)).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID_WRONG)).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisAuthorisationServiceInternal.getAuthorisationsByPaymentId(PAYMENT_ID_WRONG, PaymentAuthorisationType.CANCELLED);
        //Assert
        assertFalse(authorizationByPaymentId.isPresent());
    }

    @Test
    void getAuthorisationByPaymentIdWrongTransactionStatus() {
        //When
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID_WRONG_TRANSACTION_STATUS)).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID_WRONG_TRANSACTION_STATUS)).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisAuthorisationServiceInternal.getAuthorisationsByPaymentId(PAYMENT_ID_WRONG_TRANSACTION_STATUS, PaymentAuthorisationType.CREATED);
        //Assert
        assertFalse(authorizationByPaymentId.isPresent());
    }

    @Test
    void updateConsentAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.RECEIVED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(expectedScaStatus);
        PisAuthorization finalisedConsentAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(FINALISED_AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(Optional.of(finalisedConsentAuthorization));

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisAuthorisationServiceInternal.updatePisAuthorisation(FINALISED_AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertNotEquals(updatePisCommonPaymentPsuDataResponse.get().getScaStatus(), expectedScaStatus);
    }

    @Test
    void updateConsentAuthorisation_Success() {
        //Given
        PsuIdData psuIdData = new PsuIdData("new id", "new type", "new corporate ID", "new corporate type");
        ArgumentCaptor<PisAuthorization> argument = ArgumentCaptor.forClass(PisAuthorization.class);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus.RECEIVED);
        updatePisCommonPaymentPsuDataRequest.setPsuData(psuIdData);
        PsuData expectedPsu = psuDataMapper.mapToPsuData(psuIdData);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(PAYMENT_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(Optional.of(pisAuthorization));
        when(pisAuthorisationRepository.save(pisAuthorization)).thenReturn(pisAuthorization);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.ofNullable(expectedPsu));
        when(cmsPsuService.isPsuDataRequestCorrect(any(), any()))
            .thenReturn(true);

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisAuthorisationServiceInternal.updatePisAuthorisation(PAYMENT_ID, updatePisCommonPaymentPsuDataRequest);
        verify(pisAuthorisationRepository).save(argument.capture());
        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertTrue(argument.getValue().getPsuData().contentEquals(expectedPsu));
    }

    @Test
    void updatePisAuthorisation_receivedStatus_shouldUpdatePsuDataInPayment() {
        //Given
        ArgumentCaptor<PisAuthorization> savedAuthorisationCaptor = ArgumentCaptor.forClass(PisAuthorization.class);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest =
            buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus.RECEIVED, PSU_ID_DATA);
        List<PsuData> psuDataList = Collections.singletonList(PSU_DATA);

        when(cmsPsuService.enrichPsuData(PSU_DATA, Collections.emptyList()))
            .thenReturn(psuDataList);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.isPsuDataRequestCorrect(any(), any()))
            .thenReturn(true);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(PAYMENT_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(Optional.of(pisAuthorization));
        when(pisAuthorisationRepository.save(pisAuthorization))
            .thenReturn(pisAuthorization);

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> response =
            pisAuthorisationServiceInternal.updatePisAuthorisation(PAYMENT_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(response.isPresent());

        verify(pisAuthorisationRepository).save(savedAuthorisationCaptor.capture());
        PisAuthorization savedAuthorisation = savedAuthorisationCaptor.getValue();

        assertEquals(PSU_DATA, savedAuthorisation.getPsuData());
        assertEquals(psuDataList, savedAuthorisation.getPaymentData().getPsuDataList());
    }

    @Test
    void updatePisAuthorisation_shouldClosePreviousAuthorisations() {
        //Given
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<PisAuthorization>> savedAuthorisationsCaptor = ArgumentCaptor.forClass((Class) Iterable.class);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest =
            buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus.FINALISED, PSU_ID_DATA);
        List<PsuData> psuDataList = Collections.singletonList(PSU_DATA);

        PisCommonPaymentData pisCommonPaymentData = buildPisCommonPaymentData();
        PisAuthorization currentAuthorisation = buildPisAuthorisation(AUTHORISATION_ID, PaymentAuthorisationType.CREATED, pisCommonPaymentData);
        PisAuthorization oldAuthorisation = buildPisAuthorisation("old authorisation id", PaymentAuthorisationType.CREATED, pisCommonPaymentData);
        pisCommonPaymentData.getAuthorizations().add(currentAuthorisation);
        pisCommonPaymentData.getAuthorizations().add(oldAuthorisation);

        when(cmsPsuService.enrichPsuData(PSU_DATA, Collections.emptyList()))
            .thenReturn(psuDataList);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.isPsuDataRequestCorrect(any(), any()))
            .thenReturn(true);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(Optional.of(currentAuthorisation));
        when(pisAuthorisationRepository.save(currentAuthorisation))
            .thenReturn(currentAuthorisation);

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> response =
            pisAuthorisationServiceInternal.updatePisAuthorisation(AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(response.isPresent());

        verify(pisAuthorisationRepository).saveAll(savedAuthorisationsCaptor.capture());

        Iterable<PisAuthorization> savedAuthorisationsIterable = savedAuthorisationsCaptor.getValue();
        List<PisAuthorization> savedAuthorisations = IteratorUtils.toList(savedAuthorisationsIterable.iterator());

        assertEquals(1, savedAuthorisations.size());

        PisAuthorization savedOldAuthorisation = savedAuthorisations.get(0);
        assertEquals(oldAuthorisation.getExternalId(), savedOldAuthorisation.getExternalId());
        assertEquals(ScaStatus.FAILED, savedOldAuthorisation.getScaStatus());
    }

    @Test
    void updateConsentCancellationAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.RECEIVED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        PisAuthorization finalisedCancellationAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(expectedScaStatus);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(FINALISED_CANCELLATION_AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(Optional.of(finalisedCancellationAuthorization));

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisAuthorisationServiceInternal.updatePisCancellationAuthorisation(FINALISED_CANCELLATION_AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertNotEquals(updatePisCommonPaymentPsuDataResponse.get().getScaStatus(), expectedScaStatus);

    }

    @Test
    void createAuthorizationWithClosingPreviousAuthorisations_success() {
        //Given
        ArgumentCaptor<PisAuthorization> argument = ArgumentCaptor.forClass(PisAuthorization.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(pisAuthorisationRepository.save(any(PisAuthorization.class))).thenReturn(pisAuthorization);
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID, Arrays.asList(RCVD, PATC))).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisPaymentData.getPaymentData())).thenReturn(pisPaymentData.getPaymentData());
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(any(), any())).thenReturn(Collections.singletonList(PSU_DATA));

        // When
        Optional<CreatePisAuthorisationResponse> actual = pisAuthorisationServiceInternal.createAuthorization(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        // Then
        assertTrue(actual.isPresent());
        verify(pisAuthorisationRepository).save(argument.capture());
        PisAuthorization pisAuthorization = argument.getValue();
        assertSame(pisAuthorization.getScaStatus(), ScaStatus.PSUIDENTIFIED);
        assertEquals(TPP_REDIRECT_URI, pisAuthorization.getTppOkRedirectUri());
        assertEquals(TPP_NOK_REDIRECT_URI, pisAuthorization.getTppNokRedirectUri());
        assertEquals(pisPaymentData.getPaymentData().getInternalRequestId(), actual.get().getInternalRequestId());
    }

    @Test
    void createAuthorizationWithClosingPreviousAuthorisationsTppRedirectLinksFromAuthorisationTemplate_success() {
        //Given
        AuthorisationTemplateEntity authorisationTemplateEntity = buildAuthorisationTemplateEntity();
        PisCommonPaymentData paymentData = buildPisCommonPaymentData(authorisationTemplateEntity);
        PisPaymentData pisPaymentData = buildPaymentData(paymentData);
        CreatePisAuthorisationRequest createPisAuthorisationRequest = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PSU_ID_DATA, ScaApproach.REDIRECT, new TppRedirectUri("", ""));

        ArgumentCaptor<PisAuthorization> argument = ArgumentCaptor.forClass(PisAuthorization.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(pisAuthorisationRepository.save(any(PisAuthorization.class))).thenReturn(pisAuthorization);
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID, Arrays.asList(RCVD, PATC))).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(paymentData)).thenReturn(paymentData);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(any(), any())).thenReturn(Collections.singletonList(PSU_DATA));

        // When
        Optional<CreatePisAuthorisationResponse> actual = pisAuthorisationServiceInternal.createAuthorization(PAYMENT_ID, createPisAuthorisationRequest);

        // Then
        assertTrue(actual.isPresent());
        verify(pisAuthorisationRepository).save(argument.capture());
        PisAuthorization pisAuthorization = argument.getValue();
        assertEquals(authorisationTemplateEntity.getRedirectUri(), pisAuthorization.getTppOkRedirectUri());
        assertEquals(authorisationTemplateEntity.getNokRedirectUri(), pisAuthorization.getTppNokRedirectUri());
    }

    @Test
    void createAuthorizationCancellationWithClosingPreviousAuthorisationsTppRedirectLinksFromAuthorisationTemplate_success() {
        //Given
        AuthorisationTemplateEntity authorisationTemplateEntity = buildAuthorisationTemplateEntity();
        PisCommonPaymentData paymentData = buildPisCommonPaymentData(authorisationTemplateEntity);
        PisPaymentData pisPaymentData = buildPaymentData(paymentData);
        CreatePisAuthorisationRequest createPisAuthorisationRequest = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CANCELLED, PSU_ID_DATA, ScaApproach.REDIRECT, new TppRedirectUri("", ""));

        ArgumentCaptor<PisAuthorization> argument = ArgumentCaptor.forClass(PisAuthorization.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(pisAuthorisationRepository.save(any(PisAuthorization.class))).thenReturn(pisAuthorization);
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID, Arrays.asList(RCVD, PATC))).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(paymentData)).thenReturn(paymentData);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));

        // When
        Optional<CreatePisAuthorisationResponse> actual = pisAuthorisationServiceInternal.createAuthorization(PAYMENT_ID, createPisAuthorisationRequest);

        // Then
        assertTrue(actual.isPresent());
        verify(pisAuthorisationRepository).save(argument.capture());
        PisAuthorization pisAuthorization = argument.getValue();
        assertEquals(authorisationTemplateEntity.getCancelRedirectUri(), pisAuthorization.getTppOkRedirectUri());
        assertEquals(authorisationTemplateEntity.getCancelNokRedirectUri(), pisAuthorization.getTppNokRedirectUri());
    }

    @Test
    void getAuthorisationScaApproach() {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setScaApproach(ScaApproach.DECOUPLED);
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(Optional.of(pisAuthorization));

        Optional<AuthorisationScaApproachResponse> actual = pisAuthorisationServiceInternal.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(ScaApproach.DECOUPLED, actual.get().getScaApproach());
        verify(pisAuthorisationRepository, times(1)).findByExternalIdAndAuthorizationType(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CREATED));
    }

    @Test
    void getAuthorisationScaApproach_emptyAuthorisation() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(Optional.empty());

        Optional<AuthorisationScaApproachResponse> actual = pisAuthorisationServiceInternal.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
        verify(pisAuthorisationRepository, times(1)).findByExternalIdAndAuthorizationType(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CREATED));
    }

    @NotNull
    private AspspSettings getAspspSettings() {
        return jsonReader.getObjectFromFile("json/AspspSetting.json", AspspSettings.class);
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus status) {
        return buildUpdatePisCommonPaymentPsuDataRequest(status, null);
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus status, PsuIdData psuIdData) {
        UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorizationId(FINALISED_AUTHORISATION_ID);
        request.setScaStatus(status);
        request.setPsuData(psuIdData);
        return request;
    }

    private PisAuthorization buildFinalisedConsentAuthorisation(ScaStatus status) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(FINALISED_AUTHORISATION_ID);
        pisAuthorization.setScaStatus(status);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        return pisAuthorization;
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        return buildPisCommonPaymentData(new AuthorisationTemplateEntity());
    }

    private PisCommonPaymentData buildPisCommonPaymentData(AuthorisationTemplateEntity authorisationTemplateEntity) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setId(PIS_PAYMENT_DATA_ID);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setTransactionStatus(RCVD);
        pisCommonPaymentData.setAuthorizations(pisAuthorizationList);
        pisCommonPaymentData.setAuthorisationTemplate(authorisationTemplateEntity);
        pisCommonPaymentData.setInternalRequestId(INTERNAL_REQUEST_ID);
        return pisCommonPaymentData;
    }

    private PisAuthorization buildPisAuthorisation(String externalId, PaymentAuthorisationType authorisationType) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(externalId);
        pisAuthorization.setAuthorizationType(authorisationType);
        pisAuthorization.setScaStatus(SCA_STATUS);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        pisAuthorization.setPsuData(PSU_DATA);
        return pisAuthorization;
    }

    private PisAuthorization buildPisAuthorisation(String externalId, PaymentAuthorisationType authorisationType, PisCommonPaymentData pisCommonPaymentData) {
        PisAuthorization pisAuthorisation = new PisAuthorization();
        pisAuthorisation.setExternalId(externalId);
        pisAuthorisation.setAuthorizationType(authorisationType);
        pisAuthorisation.setScaStatus(SCA_STATUS);
        pisAuthorisation.setPaymentData(pisCommonPaymentData);
        pisAuthorisation.setPsuData(PSU_DATA);
        return pisAuthorisation;
    }

    private PisPaymentData buildPaymentData(PisCommonPaymentData pisCommonPaymentData) {
        PisPaymentData paymentData = new PisPaymentData();
        paymentData.setPaymentId(PAYMENT_ID);
        paymentData.setPaymentData(pisCommonPaymentData);
        return paymentData;
    }

    private AuthorisationTemplateEntity buildAuthorisationTemplateEntity() {
        AuthorisationTemplateEntity authorisationTemplateEntity = new AuthorisationTemplateEntity();
        authorisationTemplateEntity.setRedirectUri("template_redirect_uri");
        authorisationTemplateEntity.setNokRedirectUri("template_nok_redirect_uri");
        authorisationTemplateEntity.setCancelRedirectUri("template_cancel_redirect_uri");
        authorisationTemplateEntity.setCancelNokRedirectUri("template_cancel_nok_redirect_uri");
        return authorisationTemplateEntity;
    }
}
