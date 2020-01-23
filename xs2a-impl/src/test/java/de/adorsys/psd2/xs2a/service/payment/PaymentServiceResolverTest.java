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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.psd2.xs2a.config.factory.ReadPaymentStatusFactory;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelCertainPaymentService;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.*;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadSinglePaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadCommonPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadSinglePaymentStatusService;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceResolverTest {

    @InjectMocks
    private PaymentServiceResolver paymentServiceResolver;

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private CreateSinglePaymentService createSinglePaymentService;
    @Mock
    private CreatePeriodicPaymentService createPeriodicPaymentService;
    @Mock
    private CreateBulkPaymentService createBulkPaymentService;
    @Mock
    private ReadCommonPaymentService readCommonPaymentService;
    @Mock
    private ReadPaymentFactory readPaymentFactory;
    @Mock
    private ReadSinglePaymentService readSinglePaymentService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private CancelCommonPaymentService cancelCommonPaymentService;
    @Mock
    private CancelCertainPaymentService cancelCertainPaymentService;
    @Mock
    private ReadCommonPaymentStatusService readCommonPaymentStatusService;
    @Mock
    private ReadPaymentStatusFactory readPaymentStatusFactory;
    @Mock
    private ReadSinglePaymentStatusService readSinglePaymentStatusService;

    private PaymentInitiationParameters paymentInitiationParameters;
    private PisCommonPaymentResponse commonPaymentData;
    private PisPaymentCancellationRequest paymentCancellationRequest;
    private PisCommonPaymentResponse pisCommonPaymentResponse;

    @BeforeEach
    void setUp() {
        paymentInitiationParameters = new PaymentInitiationParameters();
        commonPaymentData = new PisCommonPaymentResponse();
        paymentCancellationRequest = new PisPaymentCancellationRequest(PaymentType.SINGLE, "", "", Boolean.TRUE, new TppRedirectUri("", ""));
        pisCommonPaymentResponse = new PisCommonPaymentResponse();
    }

    @Test
    void getCreatePaymentService_commonPayment() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(true);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createCommonPaymentService, createPaymentService);
    }

    @Test
    void getCreatePaymentService_singlePayment() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        paymentInitiationParameters.setPaymentType(PaymentType.SINGLE);
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createSinglePaymentService, createPaymentService);
    }

    @Test
    void getCreatePaymentService_periodicPayment() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        paymentInitiationParameters.setPaymentType(PaymentType.PERIODIC);
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createPeriodicPaymentService, createPaymentService);
    }

    @Test
    void getCreatePaymentService_bulkPayment() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        paymentInitiationParameters.setPaymentType(PaymentType.BULK);
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createBulkPaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreateBulkPaymentService);
    }

    @Test
    void getCreatePaymentService_noPaymentType() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentInitiationParameters.getPaymentProduct())).thenReturn(false);

        CreatePaymentService createPaymentService = paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters);
        assertEquals(createBulkPaymentService, createPaymentService);
        assertTrue(createPaymentService instanceof CreateBulkPaymentService);
    }

    @Test
    void getReadPaymentService_commonPayment() {
        commonPaymentData.setPaymentData("body".getBytes());
        ReadPaymentService readPaymentService = paymentServiceResolver.getReadPaymentService(commonPaymentData);
        assertEquals(readCommonPaymentService, readPaymentService);
    }

    @Test
    void getReadPaymentService_singlePayment() {
        commonPaymentData.setPaymentType(PaymentType.SINGLE);
        when(readPaymentFactory.getService(PaymentType.SINGLE.getValue())).thenReturn(readSinglePaymentService);

        ReadPaymentService readPaymentService = paymentServiceResolver.getReadPaymentService(commonPaymentData);
        assertTrue(readPaymentService instanceof ReadSinglePaymentService);
    }

    @Test
    void getCreatePaymentService_withOauthApproach() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.OAUTH);

        // When
        assertThrows(UnsupportedOperationException.class, () -> paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters));
    }

    @Test
    void getCreatePaymentService_withUndefinedApproach() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.UNDEFINED);

        // When
        assertThrows(UnsupportedOperationException.class, () -> paymentServiceResolver.getCreatePaymentService(paymentInitiationParameters));
    }

    @Test
    void getCancelPaymentService_commonPayment() {
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentCancellationRequest.getPaymentProduct())).thenReturn(true);

        CancelPaymentService cancelPaymentService = paymentServiceResolver.getCancelPaymentService(paymentCancellationRequest);
        assertEquals(cancelCommonPaymentService, cancelPaymentService);
    }

    @Test
    void getCancelPaymentService_certainPayment() {
        when(standardPaymentProductsResolver.isRawPaymentProduct(paymentCancellationRequest.getPaymentProduct())).thenReturn(false);

        CancelPaymentService cancelPaymentService = paymentServiceResolver.getCancelPaymentService(paymentCancellationRequest);
        assertEquals(cancelCertainPaymentService, cancelPaymentService);
    }

    @Test
    void getReadPaymentStatusService_commonPayment() {
        pisCommonPaymentResponse.setPaymentData("body".getBytes());

        ReadPaymentStatusService readPaymentStatusService = paymentServiceResolver.getReadPaymentStatusService(pisCommonPaymentResponse);
        assertEquals(readCommonPaymentStatusService, readPaymentStatusService);
    }

    @Test
    void getReadPaymentStatusService_singlePayment() {
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);
        when(readPaymentStatusFactory.getService("status-" + pisCommonPaymentResponse.getPaymentType().getValue())).thenReturn(readSinglePaymentStatusService);

        ReadPaymentStatusService readPaymentStatusService = paymentServiceResolver.getReadPaymentStatusService(pisCommonPaymentResponse);
        assertEquals(readSinglePaymentStatusService, readPaymentStatusService);
    }
}
