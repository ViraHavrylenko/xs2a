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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aToSpiPaymentMapperTest {
    private static final String PSU_ID = "psu Id";
    private static final String PSU_ID_TYPE = "psuId Type";
    private static final String PSU_CORPORATE_ID = "psu Corporate Id";
    private static final String PSU_CORPORATE_ID_TYPE = "psuCorporate Id Type";
    private static final List<PsuIdData> psuDataList = new ArrayList<>();
    private static final List<SpiPsuData> spiPsuDataList = new ArrayList<>();

    @InjectMocks
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        SpiPsuData spiPsuData = new SpiPsuData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, null);
        psuDataList.addAll(Arrays.asList(psuIdData));
        spiPsuDataList.addAll(Arrays.asList(spiPsuData));
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList)).thenReturn(spiPsuDataList);
    }

    @Test
    public void mapToSpiPayment() {
        //Given
        PisPaymentInfo paymentInfo = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/pis-payment-info.json", PisPaymentInfo.class);
        GetPisAuthorisationResponse getPisAuthorisationResponse = new GetPisAuthorisationResponse();
        getPisAuthorisationResponse.setPaymentInfo(paymentInfo);
        //When
        SpiPayment spiPayment = xs2aToSpiPaymentMapper.mapToSpiPayment(getPisAuthorisationResponse, null, null);
        SpiPaymentInfo spiPaymentInfo = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-payment-info.json", SpiPaymentInfo.class);
        //Then
        assertEquals(spiPaymentInfo, spiPayment);
    }

    private PsuIdData buildPsu(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private SpiPsuData buildSpiPsu(String psuId) {
        return new SpiPsuData(psuId, null, null, null, null);
    }
}
