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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.api.ais.AccountAccess;
import de.adorsys.psd2.consent.api.ais.CmsPsuAspspAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.Consent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.impl.AisConsentRepositoryImpl;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.ConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuAisServiceInternal;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuAisServiceTest {

    @InjectMocks
    private CmsPsuAisServiceInternal cmsPsuAisService;

    @Mock
    private ConsentJpaRepository aisConsentJpaRepository;
    @Mock
    private AisConsentRepositoryImpl aisConsentRepositoryImpl;
    @Mock
    private ConsentMapper aisConsentMapper;
    @Mock
    private AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private AisConsentAuthorization mockAisConsentAuthorization;
    @Mock
    private CmsPsuAspspAccountConsent mockCmsAisAccountConsent;
    @Mock
    private AisConsentAuthorizationSpecification aisConsentAuthorizationSpecification;
    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private ConsentService aisConsentService;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private AisConsentRequestTypeService aisConsentRequestTypeService;
    @Mock
    private ConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String AUTHORISATION_ID_NOT_EXIST = "248eae68-e4fa-4d43-8b3f-2ae2b584cdd9";
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_AUTHORISATION_ID = "6b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String TPP_OK_REDIRECT_URI = "Mock tppOkRedirectUri";
    private static final String TPP_NOK_REDIRECT_URI = "Mock tppNokRedirectUri";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String CORRECT_PSU_ID = "987654321";
    private static final String WRONG_PSU_ID = "wrong";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";

    private Consent aisConsent;
    private List<Consent> aisConsents;
    private CmsPsuAspspAccountConsent aisAccountConsent;
    private AisConsentAuthorization aisConsentAuthorization;
    private PsuIdData psuIdData;
    private PsuIdData psuIdDataWrong;
    private PsuData psuData;
    private JsonReader jsonReader;
    private AuthenticationDataHolder authenticationDataHolder;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData(CORRECT_PSU_ID);
        psuData = buildPsuData(CORRECT_PSU_ID);
        jsonReader = new JsonReader();
        aisConsent = buildConsent();

        psuIdDataWrong = buildPsuIdData(WRONG_PSU_ID);
        aisAccountConsent = buildSpiAccountConsent();
        aisConsentAuthorization = buildAisConsentAuthorisation();
        aisConsents = buildAisConsents();
        authenticationDataHolder = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);
    }

    @Test
    void updatePsuDataInConsentSuccess() throws AuthorisationIsExpiredException {
        // Given
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsentAuthorization));
        when(psuDataMapper.mapToPsuData(psuIdData)).thenReturn(psuData);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), anyList())).thenReturn(Optional.of(psuData));

        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updatePsuDataInConsent);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsentFail() throws AuthorisationIsExpiredException {
        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID_NOT_EXIST), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // When
        assertFalse(updatePsuDataInConsent);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentSuccess() {
        when(aisConsentMapper.mapToCmsPsuAspspAccountConsent(aisConsent)).thenReturn(aisAccountConsent);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        Optional<CmsPsuAspspAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), aisAccountConsent);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentFail() {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID_NOT_EXIST), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // When
        Optional<CmsPsuAspspAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consent.isPresent());
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentSuccessStatusNotChanged() {
        //Given
        ConsentStatus consentStatus = ConsentStatus.TERMINATED_BY_TPP;
        Consent aisConsentTerminatedByTpp = buildConsentByStatusAndExpireDate(consentStatus, LocalDate.now().minusDays(1));
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(aisConsentTerminatedByTpp));
        when(aisConsentMapper.mapToCmsPsuAspspAccountConsent(aisConsentTerminatedByTpp)).thenReturn(mockCmsAisAccountConsent);

        ArgumentCaptor<Consent> argument = ArgumentCaptor.forClass(Consent.class);

        // When
        Optional<CmsPsuAspspAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        verify(aisConsentMapper).mapToCmsPsuAspspAccountConsent(argument.capture());
        assertEquals(consentStatus, argument.getValue().getConsentStatus());
    }

    @Test
    void updateAuthorisationStatusSuccess() throws AuthorisationIsExpiredException {
        // When
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsentAuthorization));

        when(aisConsentAuthorisationRepository.save(aisConsentAuthorization)).thenReturn(aisConsentAuthorization);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(ScaStatus.RECEIVED, aisConsentAuthorization.getScaStatus());
    }

    @Test
    void updateAuthorisationStatusFail() throws AuthorisationIsExpiredException {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID_NOT_EXIST, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsuSuccess() {
        // Given
        when(aisConsentSpecification.byPsuDataInListAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findAll(any(Specification.class))).thenReturn(aisConsents);
        when(aisConsentMapper.mapToCmsPsuAspspAccountConsent(aisConsent)).thenReturn(aisAccountConsent);


        // When
        List<CmsPsuAspspAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(consentsForPsu.size(), aisConsents.size());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsuFail() {
        // When
        List<CmsPsuAspspAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consentsForPsu.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceId(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void confirmConsentSuccess() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(anyString()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        Consent aisConsentValid = buildConsentByStatus(ConsentStatus.VALID);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsentValid)).thenReturn(aisConsentValid);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void confirmConsentFail() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsentSuccess() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        Consent aisConsentRejected = buildConsentByStatus(ConsentStatus.REJECTED);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsentRejected)).thenReturn(aisConsentRejected);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsentFail() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsentSuccess() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        Consent aisConsentRevoked = buildConsentByStatus(ConsentStatus.REVOKED_BY_PSU);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsentRevoked)).thenReturn(aisConsentRevoked);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsentFail() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void authorisePartiallyConsentSuccess() throws WrongChecksumException {
        //Given
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        Consent aisConsent = buildConsentByStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        aisConsent.setMultilevelScaRequired(true);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsent)).thenReturn(aisConsent);
        ArgumentCaptor<Consent> argumentCaptor = ArgumentCaptor.forClass(Consent.class);
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.authorisePartiallyConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentRepositoryImpl).verifyAndSave(argumentCaptor.capture());
        Consent aisConsentActual = argumentCaptor.getValue();
        assertEquals(ConsentStatus.PARTIALLY_AUTHORISED, aisConsentActual.getConsentStatus());
        assertTrue(aisConsentActual.isMultilevelScaRequired());
    }

    @Test
    void confirmConsent_FinalisedStatus_Fail() throws WrongChecksumException {
        // When
        boolean result = cmsPsuAisService.confirmConsent(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsent_FinalisedStatus_Fail() throws WrongChecksumException {
        // When
        boolean result = cmsPsuAisService.rejectConsent(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsent_FinalisedStatus_Fail() throws WrongChecksumException {
        // When
        boolean result = cmsPsuAisService.revokeConsent(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_FinalisedStatus_Fail() throws AuthorisationIsExpiredException {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent), Optional.empty());

        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(eq(FINALISED_AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean result = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(FINALISED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentByRedirectId_Fail_AuthorisationNotFound() throws RedirectUrlIsExpiredException {
        // Given
        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // When
        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consentResponseOptional.isPresent());
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentByRedirectId_Fail_RedirectExpire() {
        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAisConsentAuthorization));

        when(mockAisConsentAuthorization.isRedirectUrlNotExpired()).thenReturn(false);
        when(aisConsentAuthorisationRepository.save(mockAisConsentAuthorization)).thenReturn(mockAisConsentAuthorization);

        // When
        assertThrows(
            RedirectUrlIsExpiredException.class,
            () -> cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)
        );

        verify(mockAisConsentAuthorization).setScaStatus(ScaStatus.FAILED);
    }

    @Test
    void getConsentByRedirectId_Fail_NullAisConsent() throws RedirectUrlIsExpiredException {
        // Given
        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAisConsentAuthorization));

        when(mockAisConsentAuthorization.isRedirectUrlNotExpired()).thenReturn(true);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(null);

        // When
        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consentResponseOptional.isPresent());
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentByRedirectId_Success() throws RedirectUrlIsExpiredException {
        // Given
        when(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAisConsentAuthorization));

        when(mockAisConsentAuthorization.isRedirectUrlNotExpired()).thenReturn(true);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(aisConsent);
        when(aisConsentMapper.mapToCmsPsuAspspAccountConsent(aisConsent)).thenReturn(mockCmsAisAccountConsent);
        when(mockAisConsentAuthorization.getTppOkRedirectUri()).thenReturn(TPP_OK_REDIRECT_URI);
        when(mockAisConsentAuthorization.getTppNokRedirectUri()).thenReturn(TPP_NOK_REDIRECT_URI);

        // When
        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consentResponseOptional.isPresent());
        verifyCmsAisConsentResponse(consentResponseOptional.get());
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAccountAccessInConsent_Success() throws WrongChecksumException {
        // Given
        int frequencyPerDay = 777;
        String iban = "DE67597874259856475273";
        Currency currency = Currency.getInstance("EUR");
        LocalDate validUntil = LocalDate.now();
        AccountReference accountReference = getAccountReference(iban, currency);
        AccountAccess aisAccountAccess = getAisAccountAccess(accountReference);
        Set<AspspAccountAccess> aspspAccountAccesses = getAspspAccountAccesses(aisAccountAccess);
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, validUntil, frequencyPerDay, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<Consent> argument = ArgumentCaptor.forClass(Consent.class);
        when(aisConsentMapper.mapAspspAccountAccesses(aisAccountAccess)).thenReturn(aspspAccountAccesses);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        List<AspspAccountAccess> aspspAccountAccessesChecked = argument.getValue().getAspspAccountAccesses();
        assertSame(aspspAccountAccessesChecked.size(), aspspAccountAccesses.size());
        assertSame(aspspAccountAccessesChecked.get(0).getAccountIdentifier(), iban);
        assertSame(aspspAccountAccessesChecked.get(0).getCurrency(), currency);
        assertSame(argument.getValue().getValidUntil(), validUntil);
        assertEquals(argument.getValue().getAllowedFrequencyPerDay(), frequencyPerDay);
        assertEquals(getUsageCounter(argument.getValue()), frequencyPerDay);
        assertTrue(argument.getValue().isRecurringIndicator());
        assertTrue(argument.getValue().isCombinedServiceIndicator());
        assertNotEquals(AisConsentRequestType.BANK_OFFERED, argument.getValue().getAisConsentRequestType());
        assertTrue(saved);
    }

    @Test
    void updateAccountAccessInConsent_NoAdditionalAccountInformation_Success() throws WrongChecksumException {
        // Given
        AccountReference accountReference = getAccountReference("DE67597874259856475273", Currency.getInstance("EUR"));
        AccountAccess aisAccountAccess = getAisAccountAccess(accountReference);
        Set<AspspAccountAccess> aspspAccountAccesses = getAspspAccountAccesses(aisAccountAccess);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, LocalDate.now(), 777, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<Consent> argument = ArgumentCaptor.forClass(Consent.class);
        when(aisConsentMapper.mapAspspAccountAccesses(aisAccountAccess)).thenReturn(aspspAccountAccesses);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        Consent aisConsent = argument.getValue();
        List<AspspAccountAccess> aspspAccountAccessesChecked = aisConsent.getAspspAccountAccesses();

        Function<TypeAccess, Long> countAccessesByType = typeAccess -> aspspAccountAccessesChecked.stream()
                                                                           .map(AspspAccountAccess::getTypeAccess)
                                                                           .filter(type -> type.equals(typeAccess))
                                                                           .count();

        assertEquals(0, countAccessesByType.apply(TypeAccess.OWNER_NAME).longValue());
        assertEquals(0, countAccessesByType.apply(TypeAccess.OWNER_ADDRESS).longValue());
        assertEquals(AdditionalAccountInformationType.NONE, aisConsent.getOwnerNameType());
        assertTrue(saved);
    }

    @Test
    void updateAccountAccessInConsent_AdditionalAccountInformation_Success() throws WrongChecksumException {
        // Given
        AccountReference accountReference = getAccountReference("DE67597874259856475273", Currency.getInstance("EUR"));
        AccountAccess aisAccountAccess = getAisAccountAccessWithAdditionalAccountInformation(accountReference);
        Set<AspspAccountAccess> aspspAccountAccesses = getAspspAccountAccesses(aisAccountAccess);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, LocalDate.now(), 777, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<Consent> argument = ArgumentCaptor.forClass(Consent.class);
        when(aisConsentMapper.mapAspspAccountAccesses(aisAccountAccess)).thenReturn(aspspAccountAccesses);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        Consent aisConsent = argument.getValue();
        List<AspspAccountAccess> aspspAccountAccessesChecked = aisConsent.getAspspAccountAccesses();

        Function<TypeAccess, Long> countAccessesByType = typeAccess -> aspspAccountAccessesChecked.stream()
                                                                           .map(AspspAccountAccess::getTypeAccess)
                                                                           .filter(type -> type.equals(typeAccess))
                                                                           .count();

        assertEquals(1L, countAccessesByType.apply(TypeAccess.OWNER_NAME).longValue());
        assertEquals(AdditionalAccountInformationType.DEDICATED_ACCOUNTS, aisConsent.getOwnerNameType());
        assertTrue(saved);
    }

    @Test
    void updateAccountAccessInConsent_AdditionalAccountInformation_AllAvailableAccounts_Success() throws WrongChecksumException {
        // Given
        AccountReference accountReference = getAccountReference("DE67597874259856475273", Currency.getInstance("EUR"));
        AccountAccess aisAccountAccess = getAisAccountAccessWithAdditionalAccountInformationAllAvailableAccounts(accountReference);
        Set<AspspAccountAccess> aspspAccountAccesses = getAspspAccountAccesses(aisAccountAccess);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, LocalDate.now(), 777, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<Consent> argument = ArgumentCaptor.forClass(Consent.class);
        when(aisConsentMapper.mapAspspAccountAccesses(aisAccountAccess)).thenReturn(aspspAccountAccesses);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        Consent aisConsent = argument.getValue();
        List<AspspAccountAccess> aspspAccountAccessesChecked = aisConsent.getAspspAccountAccesses();

        Function<TypeAccess, Long> count = typeAccess -> aspspAccountAccessesChecked.stream()
                                                             .map(AspspAccountAccess::getTypeAccess)
                                                             .filter(type -> type.equals(typeAccess))
                                                             .count();

        assertEquals(0, count.apply(TypeAccess.OWNER_NAME).longValue());
        assertEquals(0, count.apply(TypeAccess.OWNER_ADDRESS).longValue());
        assertEquals(AdditionalAccountInformationType.ALL_AVAILABLE_ACCOUNTS, aisConsent.getOwnerNameType());
        assertTrue(saved);
    }

    @Test
    void getPsuDataAuthorisations_Success() {
        // Given
        Consent consent = buildAisConsentWithFinalisedAuthorisation();
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(consent));

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(1, actualResult.get().size());
        assertEquals(ScaStatus.FINALISED, actualResult.get().get(0).getScaStatus());
    }

    @Test
    void getPsuDataAuthorisationsEmptyPsuData_Success() {
        // Given
        Consent consent = buildAisConsentWithFinalisedAuthorisationNoPsuData();
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(consent));

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().isEmpty());
    }

    @Test
    void saveAccountAccessInConsent_Consent_Finalised_Failed() {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(""))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.ofNullable(aisConsent));

        // Given
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, "");
        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_Consent_Unknown_Failed() {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID_NOT_EXIST), eq(""))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        // Given
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID_NOT_EXIST, accountAccessRequest, "");
        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_AccessIsNull() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(aisConsent));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_InvalidValidUntil() {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(aisConsentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(aisConsent));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, LocalDate.now().minusDays(1), 1, null, null);

        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);
        assertFalse(saved);
    }

    private void verifyCmsAisConsentResponse(CmsAisConsentResponse cmsAisConsentResponse) {
        assertEquals(mockCmsAisAccountConsent, cmsAisConsentResponse.getAccountConsent());
        assertEquals(AUTHORISATION_ID, cmsAisConsentResponse.getAuthorisationId());
        assertEquals(TPP_NOK_REDIRECT_URI, cmsAisConsentResponse.getTppNokRedirectUri());
        assertEquals(TPP_OK_REDIRECT_URI, cmsAisConsentResponse.getTppOkRedirectUri());
    }

    private int getUsageCounter(Consent aisConsent) {
        Integer usage = aisConsent.getUsages().stream()
                            .filter(consent -> LocalDate.now().isEqual(consent.getUsageDate()))
                            .findFirst()
                            .map(AisConsentUsage::getUsage)
                            .orElse(0);

        return Math.max(aisConsent.getAllowedFrequencyPerDay() - usage, 0);
    }

    private Set<AspspAccountAccess> getAspspAccountAccesses(AccountAccess aisAccountAccess) {
        Set<AspspAccountAccess> aspspAccountAccesses = new HashSet<>();
        aspspAccountAccesses.add(mapToAccountInfo(aisAccountAccess.getAccounts().get(0), TypeAccess.ACCOUNT));
        aspspAccountAccesses.add(mapToAccountInfo(aisAccountAccess.getBalances().get(0), TypeAccess.BALANCE));
        aspspAccountAccesses.add(mapToAccountInfo(aisAccountAccess.getTransactions().get(0), TypeAccess.TRANSACTION));
        AdditionalInformationAccess info = aisAccountAccess.getAccountAdditionalInformationAccess();
        if (info != null) {
            List<AccountReference> ownerName = info.getOwnerName();
            if (!ownerName.isEmpty()) {
                aspspAccountAccesses.add(mapToAccountInfo(ownerName.get(0), TypeAccess.OWNER_NAME));
            }
        }
        return aspspAccountAccesses;
    }

    private AspspAccountAccess mapToAccountInfo(AccountReference accountReference, TypeAccess typeAccess) {
        AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();

        return new AspspAccountAccess(selector.getAccountValue(),
                                      typeAccess,
                                      selector.getAccountReferenceType(),
                                      accountReference.getCurrency(),
                                      accountReference.getResourceId(),
                                      accountReference.getAspspAccountId());
    }

    private Consent buildAisConsentWithFinalisedAuthorisation() {
        Consent consent = buildConsent();
        AisConsentAuthorization finalisedAuthorisation = buildFinalisedAuthorisation();
        finalisedAuthorisation.setPsuData(psuData);
        consent.setAuthorizations(Collections.singletonList(finalisedAuthorisation));
        return consent;
    }

    private Consent buildAisConsentWithFinalisedAuthorisationNoPsuData() {
        Consent consent = buildConsent();
        consent.setAuthorizations(Collections.singletonList(buildFinalisedAuthorisation()));
        return consent;
    }

    private List<Consent> buildAisConsents() {
        return Arrays.asList(aisConsent, aisConsent, aisConsent);
    }

    private AisConsentAuthorization buildAisConsentAuthorisation() {
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setExternalId(AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        aisConsentAuthorization.setConsent(buildConsent());
        aisConsentAuthorization.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));
        aisConsentAuthorization.setScaAuthenticationData(AUTHENTICATION_DATA);
        aisConsentAuthorization.setAuthenticationMethodId(METHOD_ID);
        return aisConsentAuthorization;
    }

    private AisConsentAuthorization buildFinalisedAuthorisation() {
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setExternalId(FINALISED_AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.FINALISED);
        return aisConsentAuthorization;
    }

    private Consent buildConsent() {
        Consent aisConsent = jsonReader.getObjectFromFile("json/AisConsent.json", Consent.class);

        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setValidUntil(LocalDate.now().plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuDataList(Collections.singletonList(psuData));
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setAisConsentRequestType(AisConsentRequestType.BANK_OFFERED);

        return aisConsent;
    }

    private Consent buildConsentByStatus(ConsentStatus status) {
        Consent aisConsent = buildConsent();
        aisConsent.setConsentStatus(status);
        return aisConsent;
    }

    private Consent buildConsentByStatusAndExpireDate(ConsentStatus status, LocalDate validUntil) {
        Consent aisConsent = buildConsent();
        aisConsent.setConsentStatus(status);
        aisConsent.setValidUntil(validUntil);
        return aisConsent;
    }

    private PsuData buildPsuData(String psuId) {
        return new PsuData(psuId, "", "", "", "");
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, "", "", "", "");
    }

    private CmsPsuAspspAccountConsent buildSpiAccountConsent() {
        return new CmsPsuAspspAccountConsent(aisConsent.getId().toString(),
                                             null, false,
                                             null, null, 0,
                                             null, null,
                                             false, false, null, null, null, null, false, Collections.emptyList(), Collections.emptyMap(), OffsetDateTime.now(),
                                             OffsetDateTime.now());
    }

    private AccountAccess getAisAccountAccess(AccountReference accountReference) {
        return new AccountAccess(
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            null,
            null,
            null, null);
    }

    private AccountAccess getAisAccountAccessWithAdditionalAccountInformation(AccountReference accountReference) {
        return new AccountAccess(
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            null,
            null,
            null,
            new AdditionalInformationAccess(Collections.singletonList(accountReference)));
    }

    private AccountAccess getAisAccountAccessWithAdditionalAccountInformationAllAvailableAccounts(AccountReference accountReference) {
        return new AccountAccess(
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            null,
            null,
            null,
            new AdditionalInformationAccess(Collections.emptyList()));
    }

    private AccountReference getAccountReference(String iban, Currency currency) {
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(iban);
        accountReference.setCurrency(currency);
        return accountReference;
    }
}

