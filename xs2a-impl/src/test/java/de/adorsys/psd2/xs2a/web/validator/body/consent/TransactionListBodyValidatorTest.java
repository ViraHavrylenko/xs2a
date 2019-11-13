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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

public class TransactionListBodyValidatorTest {

    private TransactionListBodyValidator transactionListBodyValidator;
    private MessageError messageError;
    private MockHttpServletRequest request;

    @Before
    public void init() {
        messageError = new MessageError();
        request = new MockHttpServletRequest();

        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        transactionListBodyValidator = new TransactionListBodyValidator(errorBuildingService, new Xs2aObjectMapper());
    }

    @Test
    public void validate_successForAnyBookingStatusWhenJson() {
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        for (BookingStatus bookingStatus : BookingStatus.values()) {
            request.setParameter(TransactionListBodyValidator.BOOKING_STATUS_PARAM, bookingStatus.getValue());

            transactionListBodyValidator.validate(request, messageError);
            assertTrue(messageError.getTppMessages().isEmpty());
        }
    }

    @Test
    public void validate_requestedFormatInvalidError() {
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
        request.setParameter(TransactionListBodyValidator.BOOKING_STATUS_PARAM, BookingStatus.INFORMATION.getValue());

        transactionListBodyValidator.validate(request, messageError);

        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageErrorCode.REQUESTED_FORMATS_INVALID, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    public void validate_successWithOtherBookingStatuses() {
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);

        for (BookingStatus bookingStatus : BookingStatus.values()) {
            if (BookingStatus.INFORMATION != bookingStatus) {
                request.setParameter(TransactionListBodyValidator.BOOKING_STATUS_PARAM, bookingStatus.getValue());

                transactionListBodyValidator.validate(request, messageError);
                assertTrue(messageError.getTppMessages().isEmpty());
            }
        }
    }

    @Test
    public void validate_noAcceptHeader() {
        transactionListBodyValidator.validate(request, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }
}
