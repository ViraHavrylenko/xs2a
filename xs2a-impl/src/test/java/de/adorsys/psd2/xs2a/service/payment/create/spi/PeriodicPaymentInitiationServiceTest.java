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

package de.adorsys.psd2.xs2a.service.payment.create.spi;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodicPaymentInitiationServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData();

    private static final PeriodicPayment PERIODIC_PAYMENT = new PeriodicPayment();
    private static final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(PRODUCT);
    private static final SpiPeriodicPaymentInitiationResponse SPI_PERIODIC_PAYMENT_RESPONSE = buildSpiPeriodicPaymentInitiationResponse();
    private static final SpiResponse<SpiPeriodicPaymentInitiationResponse> SPI_PERIODIC_RESPONSE = buildSpiResponse();
    private static final PeriodicPaymentInitiationResponse PERIODIC_PAYMENT_RESPONSE = new PeriodicPaymentInitiationResponse();

    private static final TppMessage FORMAT_ERROR = new TppMessage(MessageErrorCode.FORMAT_ERROR);
    private static final ErrorHolder EXPECTED_ERROR = ErrorHolder.builder(ErrorType.PIS_404)
                                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                                          .build();

    @InjectMocks
    private PeriodicPaymentInitiationService periodicPaymentService;
    @Mock
    private PeriodicPaymentSpi periodicPaymentSpi;
    @Mock
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    @Mock
    private SpiToXs2aPaymentMapper spiToXs2aPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private InitialSpiAspspConsentDataProvider initialSpiAspspConsentDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @BeforeEach
    void init() {
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(initialSpiAspspConsentDataProvider);
    }

    @Test
    void createPeriodicPayment_success() {
        // Given
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);
        when(periodicPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_PERIODIC_PAYMENT, initialSpiAspspConsentDataProvider))
            .thenReturn(SPI_PERIODIC_RESPONSE);
        when(spiToXs2aPaymentMapper.mapToPaymentInitiateResponse(eq(SPI_PERIODIC_PAYMENT_RESPONSE), eq(initialSpiAspspConsentDataProvider)))
            .thenReturn(PERIODIC_PAYMENT_RESPONSE);

        // When
        PaymentInitiationResponse actualResponse = periodicPaymentService.initiatePayment(PERIODIC_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isNull();
        assertThat(actualResponse).isEqualTo(PERIODIC_PAYMENT_RESPONSE);
    }

    @Test
    void createPeriodicPayment_periodicPaymentSpi_initiatePayment_failed() {
        // Given
        SpiResponse<SpiPeriodicPaymentInitiationResponse> expectedFailureResponse = SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                                                                                        .error(FORMAT_ERROR)
                                                                                        .build();
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(PERIODIC_PAYMENT, PRODUCT))
            .thenReturn(SPI_PERIODIC_PAYMENT);
        when(periodicPaymentSpi.initiatePayment(SPI_CONTEXT_DATA, SPI_PERIODIC_PAYMENT, initialSpiAspspConsentDataProvider))
            .thenReturn(expectedFailureResponse);
        when(spiErrorMapper.mapToErrorHolder(expectedFailureResponse, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);

        // When
        PaymentInitiationResponse actualResponse = periodicPaymentService.initiatePayment(PERIODIC_PAYMENT, PRODUCT, PSU_DATA);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isNotNull();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    private static SpiContextData getSpiContextData() {
        return new SpiContextData(
            new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress"),
            new TppInfo(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );
    }

    private static SpiPeriodicPaymentInitiationResponse buildSpiPeriodicPaymentInitiationResponse() {
        SpiPeriodicPaymentInitiationResponse response = new SpiPeriodicPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setAspspAccountId(ASPSP_ACCOUNT_ID);
        return response;
    }

    private static SpiResponse<SpiPeriodicPaymentInitiationResponse> buildSpiResponse() {
        return SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                   .payload(SPI_PERIODIC_PAYMENT_RESPONSE)
                   .build();
    }
}
