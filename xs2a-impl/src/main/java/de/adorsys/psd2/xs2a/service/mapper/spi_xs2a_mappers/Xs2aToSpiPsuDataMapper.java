/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class Xs2aToSpiPsuDataMapper {

    public List<SpiPsuData> mapToSpiPsuDataList(List<PsuIdData> psuIdDataList) {
        if (psuIdDataList == null) {
            return Collections.emptyList();
        }

        return psuIdDataList.stream()
                   .map(this::mapToSpiPsuData)
                   .collect(Collectors.toList());
    }

    public SpiPsuData mapToSpiPsuData(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .map(psu -> {
                       if (psu.getAdditionalPsuIdData() == null) {
                           return new SpiPsuData(psu.getPsuId(), psu.getPsuIdType(), psu.getPsuCorporateId(), psu.getPsuCorporateIdType(), null, null, null, null, null, null, null, null, null, null);
                       }

                       AdditionalPsuIdData additionalPsuIdData = psu.getAdditionalPsuIdData();
                       String psuDeviceId = additionalPsuIdData.getPsuDeviceId();
                       return new SpiPsuData(psu.getPsuId(), psu.getPsuIdType(), psu.getPsuCorporateId(), psu.getPsuCorporateIdType(), additionalPsuIdData.getPsuIpAddress(), additionalPsuIdData.getPsuIpPort(), additionalPsuIdData.getPsuUserAgent(), additionalPsuIdData.getPsuGeoLocation(), additionalPsuIdData.getPsuAccept(), additionalPsuIdData.getPsuAcceptCharset(), additionalPsuIdData.getPsuAcceptEncoding(), additionalPsuIdData.getPsuAcceptLanguage(), additionalPsuIdData.getPsuHttpMethod(), psuDeviceId == null ? null : UUID.fromString(psuDeviceId));
                   })
                   .orElseGet(() -> new SpiPsuData(null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    }


}
