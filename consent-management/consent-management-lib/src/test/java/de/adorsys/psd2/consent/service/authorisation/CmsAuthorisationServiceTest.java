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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAuthorisationServiceTest {
    private static final String PARENT_ID = "payment ID";
    private static final String AUTHORISATION_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final PsuData PSU_DATA = new PsuData("PSU_ID", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("PSU_ID", null, null, null, null);

    @InjectMocks
    private PisAuthService service;

    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;

    @Mock
    private AspspSettings aspspSettings;
    @Mock
    private CommonAspspProfileSetting commonAspspProfileSetting;

    @Test
    void getAuthorisationsByParentId() {
        service.getAuthorisationsByParentId(PARENT_ID);
        verify(authorisationRepository, times(1)).findAllByParentExternalIdAndAuthorisationType(PARENT_ID, AuthorisationType.PIS_CREATION);
    }

    @Test
    void getAuthorisationById() {
        service.getAuthorisationById(AUTHORISATION_ID);
        verify(authorisationRepository, times(1)).findByExternalIdAndAuthorisationType(AUTHORISATION_ID, AuthorisationType.PIS_CREATION);
    }

    @Test
    void saveAuthorisation() {
        PisCommonPaymentData authorisationParent = new PisCommonPaymentData();
        authorisationParent.setPsuDataList(Collections.singletonList(PSU_DATA));
        CreateAuthorisationRequest request = new CreateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);

        when(psuDataMapper.mapToPsuData(request.getPsuData())).thenReturn(PSU_DATA);
        when(cmsPsuService.definePsuDataForAuthorisation(PSU_DATA, Collections.singletonList(PSU_DATA)))
            .thenReturn(Optional.of(PSU_DATA));
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(aspspSettings.getCommon()).thenReturn(commonAspspProfileSetting);
        when(commonAspspProfileSetting.getRedirectUrlExpirationTimeMs()).thenReturn(100L);
        when(commonAspspProfileSetting.getAuthorisationExpirationTimeMs()).thenReturn(200L);

        AuthorisationEntity entity = new AuthorisationEntity();
        when(authorisationMapper.prepareAuthorisationEntity(authorisationParent, request, Optional.of(PSU_DATA), AuthorisationType.PIS_CREATION, 100L, 200L))
            .thenReturn(entity);

        service.saveAuthorisation(request, authorisationParent);

        verify(authorisationRepository, times(1)).save(entity);
    }

    @Test
    void doUpdateAuthorisation_success() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setScaStatus(ScaStatus.RECEIVED);
        entity.setPsuData(PSU_DATA);
        entity.setParentExternalId(PARENT_ID);
        entity.setAuthenticationMethodId("111");

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);
        request.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        request.setAuthenticationMethodId("222");

        assertNotEquals(request.getScaStatus(), entity.getScaStatus());
        assertNotEquals(request.getAuthenticationMethodId(), entity.getAuthenticationMethodId());

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(cmsPsuService.isPsuDataRequestCorrect(PSU_DATA, PSU_DATA)).thenReturn(true);

        PisCommonPaymentData authorisationParent = new PisCommonPaymentData();
        authorisationParent.setPsuDataList(Collections.singletonList(PSU_DATA));
        when(pisCommonPaymentDataRepository.findByPaymentId(PARENT_ID)).thenReturn(Optional.of(authorisationParent));
        when(cmsPsuService.definePsuDataForAuthorisation(PSU_DATA, Collections.singletonList(PSU_DATA)))
            .thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(PSU_DATA, Collections.singletonList(PSU_DATA)))
            .thenReturn(Collections.singletonList(PSU_DATA));

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationRepository, times(1)).save(entity);

        assertEquals(request.getScaStatus(), entity.getScaStatus());
        assertEquals("222", entity.getAuthenticationMethodId());
    }

    @Test
    void doUpdateAuthorisation_authorisationParentNotFound() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setScaStatus(ScaStatus.RECEIVED);
        entity.setPsuData(PSU_DATA);
        entity.setParentExternalId(PARENT_ID);

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);
        request.setScaStatus(ScaStatus.PSUIDENTIFIED);
        assertNotEquals(request.getScaStatus(), entity.getScaStatus());

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(cmsPsuService.isPsuDataRequestCorrect(PSU_DATA, PSU_DATA)).thenReturn(true);

        PisCommonPaymentData authorisationParent = new PisCommonPaymentData();
        authorisationParent.setPsuDataList(Collections.singletonList(PSU_DATA));
        when(pisCommonPaymentDataRepository.findByPaymentId(PARENT_ID)).thenReturn(Optional.empty());

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationRepository, never()).save(entity);
    }

    @Test
    void doUpdateAuthorisation_isNotPsuDataRequestCorrect() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setPsuData(PSU_DATA);
        entity.setScaStatus(ScaStatus.RECEIVED);

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(cmsPsuService.isPsuDataRequestCorrect(PSU_DATA, PSU_DATA)).thenReturn(false);

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationRepository, never()).save(entity);
    }

    @Test
    void doUpdateAuthorisation_scaStatusNotReceived() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setScaStatus(ScaStatus.PSUIDENTIFIED);

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationRepository, never()).save(entity);
    }
}
