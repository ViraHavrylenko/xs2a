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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
// ToDo: cover with tests https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
public class CmsConsentMapper {
    private final AspspProfileService aspspProfileService;
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final ConsentTppInformationMapper consentTppInformationMapper;
    private final PsuDataMapper psuDataMapper;
    private final AuthorisationMapper authorisationMapper;

    public CmsConsent mapToCmsConsent(ConsentEntity entity, List<AuthorisationEntity> authorisations, Map<String, Integer> usages) {
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setId(entity.getExternalId());
        cmsConsent.setConsentData(entity.getData());
        cmsConsent.setChecksum(entity.getChecksum());
        cmsConsent.setConsentStatus(entity.getConsentStatus());
        cmsConsent.setConsentType(ConsentType.getByValue(entity.getConsentType()));
        cmsConsent.setTppInformation(consentTppInformationMapper.mapToConsentTppInformation(entity.getTppInformation()));
        cmsConsent.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()));
        cmsConsent.setInternalRequestId(entity.getInternalRequestId());
        cmsConsent.setFrequencyPerDay(entity.getFrequencyPerDay());
        cmsConsent.setValidUntil(entity.getValidUntil());
        cmsConsent.setPsuIdDataList(psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()));
        cmsConsent.setRecurringIndicator(entity.isRecurringIndicator());
        cmsConsent.setMultilevelScaRequired(entity.isMultilevelScaRequired());
        cmsConsent.setExpireDate(entity.getExpireDate());
        cmsConsent.setLastActionDate(entity.getLastActionDate());
        cmsConsent.setAuthorisations(authorisationMapper.mapToAuthorisations(authorisations));
        cmsConsent.setUsages(usages);
        return cmsConsent;
    }

    public ConsentEntity mapToNewConsentEntity(CmsConsent cmsConsent) {
        ConsentEntity entity = new ConsentEntity();
        entity.setData(cmsConsent.getConsentData());
        entity.setChecksum(cmsConsent.getChecksum());
        entity.setExternalId(UUID.randomUUID().toString());
        entity.setConsentStatus(ConsentStatus.RECEIVED);
        entity.setConsentType(cmsConsent.getConsentType().getName());
        entity.setFrequencyPerDay(cmsConsent.getFrequencyPerDay());
        entity.setMultilevelScaRequired(cmsConsent.isMultilevelScaRequired());
        entity.setRequestDateTime(LocalDateTime.now());
        entity.setValidUntil(adjustExpireDate(cmsConsent.getValidUntil()));
        entity.setExpireDate(adjustExpireDate(cmsConsent.getExpireDate()));
        entity.setPsuDataList(psuDataMapper.mapToPsuDataList(cmsConsent.getPsuIdDataList()));
        entity.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplateEntity(cmsConsent.getAuthorisationTemplate()));
        entity.setRecurringIndicator(cmsConsent.isRecurringIndicator());
        entity.setLastActionDate(LocalDate.now());
        entity.setInternalRequestId(cmsConsent.getInternalRequestId());
        entity.setTppInformation(consentTppInformationMapper.mapToConsentTppInformationEntity(cmsConsent.getTppInformation()));
        return entity;
    }

    private LocalDate adjustExpireDate(LocalDate validUntil) {
        // ToDo: consider checking consent type here or moving adjustment to XS2A https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
        int lifetime = aspspProfileService.getAspspSettings().getAis().getConsentTypes().getMaxConsentValidityDays();
        if (lifetime <= 0) {
            return validUntil;
        }

        //Expire date is inclusive and TPP can access AIS consent from current date
        LocalDate lifeTimeDate = LocalDate.now().plusDays(lifetime - 1L);
        return lifeTimeDate.isBefore(validUntil) ? lifeTimeDate : validUntil;
    }
}