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

package de.adorsys.psd2.xs2a.web.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.DayOfExecution;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodicPaymentJsonPartConverterTest {
    private static final String SERIALISED_BODY = "properly serialised body";
    private static final String MALFORMED_SERIALISED_BODY = "malformed body";

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @InjectMocks
    private PeriodicPaymentJsonPartConverter periodicPaymentJsonPartConverter;

    @Test
    void convert_withCorrectBody_shouldReturnObject() throws IOException {
        // Given
        when(xs2aObjectMapper.readValue(SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class))
            .thenReturn(buildPeriodicPaymentJson());

        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson expected = buildPeriodicPaymentJson();

        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(SERIALISED_BODY);

        // Then
        verify(xs2aObjectMapper).readValue(SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class);
        assertEquals(expected, actualResult);
    }

    @Test
    void convert_withMalformedBody_shouldReturnNull() throws JsonProcessingException {
        // Given
        when(xs2aObjectMapper.readValue(MALFORMED_SERIALISED_BODY, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class))
            .thenReturn(null);

        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(MALFORMED_SERIALISED_BODY);

        // Then
        assertNull(actualResult);
    }

    @Test
    void convert_withNullString_shouldReturnNull() {
        // When
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson actualResult = periodicPaymentJsonPartConverter.convert(null);

        // Then
        assertNull(actualResult);
    }

    private PeriodicPaymentInitiationXmlPart2StandingorderTypeJson buildPeriodicPaymentJson() {
        PeriodicPaymentInitiationXmlPart2StandingorderTypeJson periodicPaymentJson = new PeriodicPaymentInitiationXmlPart2StandingorderTypeJson();
        periodicPaymentJson.setDayOfExecution(DayOfExecution._2);
        periodicPaymentJson.startDate(LocalDate.of(2019, 4, 9));
        return periodicPaymentJson;
    }
}
