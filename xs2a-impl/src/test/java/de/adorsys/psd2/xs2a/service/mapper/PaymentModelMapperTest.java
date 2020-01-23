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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentModelMapperTest {

    private static final boolean BATCH_BOOKING_PREFERRED = true;
    private static final String END_TO_END_IDENTIFICATION = "123456789";
    private static final String INSTRUCTION_IDENTIFICATION = "INSTRUCTION_IDENTIFICATION";
    private static final String IBAN = "DE1234567890";
    private static final String CURRENCY = "EUR";
    private static final String STANDARD_PAYMENT_TYPE = "sepa-credit-transfers";
    private static final String NON_STANDARD_PAYMENT_TYPE = "pain.001-sepa-credit-transfers";
    private static final String NON_STANDARD_PAYMENT_DATA_STRING = "Test payment data";
    private static final String CREDITOR_AGENT = "TestAgent";
    private static final String CREDITOR_NAME = "TestAgentName";
    private static final String REMITTANCE_INFORMATION_UNSTRUCTED = "Test remmitanse info";
    private static final LocalDate START_DATE = LocalDate.of(2020, 1, 2);
    private static final LocalDate END_DATE = LocalDate.of(2020, 6, 2);
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.of(2020, 2, 15);
    private static final DayOfExecution PSD2_DAY_OF_EXECUTION = DayOfExecution._2;
    private static final ExecutionRule PSD2_EXECUTION_RULE = ExecutionRule.FOLLOWING;
    private static final de.adorsys.psd2.model.FrequencyCode PSD2_FREQUENCY_CODE = de.adorsys.psd2.model.FrequencyCode.DAILY;
    private static final PisDayOfExecution XS2A_DAY_OF_EXECUTION = PisDayOfExecution._2;
    private static final PisExecutionRule XS2A_EXECUTION_RULE = PisExecutionRule.FOLLOWING;
    private static final de.adorsys.psd2.xs2a.core.pis.FrequencyCode XS2A_FREQUENCY_CODE = FrequencyCode.DAILY;
    private static final JsonReader jsonReader = new JsonReader();
    private static final String ULTIMATE_DEBTOR = "ultimate debtor";
    private static final String ULTIMATE_CREDITOR = "ultimate creditor";
    private static final PurposeCode PURPOSE_CODE = PurposeCode.fromValue("BKDF");
    private static final Remittance REMITTANCE = jsonReader.getObjectFromFile("json/service/mapper/remittance.json", Remittance.class);

    @InjectMocks
    PaymentModelMapperPsd2 paymentModelMapperPsd2;

    @Mock
    private AmountModelMapper amountModelMapper;

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;

    @Spy
    AccountModelMapper accountModelMapper = new AccountModelMapperImpl();

    @Spy
    Xs2aAddressMapper xs2aAddressMapper = Mappers.getMapper(Xs2aAddressMapper.class);

    @Spy
    PurposeCodeMapper purposeCodeMapper = Mappers.getMapper(PurposeCodeMapper.class);

    @Spy
    RemittanceMapper remittanceMapper = Mappers.getMapper(RemittanceMapper.class);

    @Test
    void mapToGetPaymentResponse12_Single_success() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(STANDARD_PAYMENT_TYPE)).thenReturn(false);
        when(amountModelMapper.mapToAmount(buildXs2aAmount())).thenReturn(getAmount12(true, true));

        //When
        PaymentInitiationWithStatusResponse result = (PaymentInitiationWithStatusResponse) paymentModelMapperPsd2.mapToGetPaymentResponse(buildSinglePayment(TransactionStatus.RCVD), SINGLE, STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getEndToEndIdentification()).isEqualTo(END_TO_END_IDENTIFICATION);
        assertThat(result.getInstructionIdentification()).isEqualTo(INSTRUCTION_IDENTIFICATION);
        assertThat(result.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getInstructedAmount()).isEqualTo(getAmount12(true, true));
        assertThat(result.getCreditorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getCreditorAgent()).isEqualTo(CREDITOR_AGENT);
        assertThat(result.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(result.getCreditorAddress()).isEqualTo(getAddress12(true, true, true, true, true));
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION_UNSTRUCTED);
        assertThat(result.getTransactionStatus()).isEqualTo(de.adorsys.psd2.model.TransactionStatus.RCVD);
        assertThat(result.getUltimateDebtor()).isEqualTo(ULTIMATE_DEBTOR);
        assertThat(result.getUltimateCreditor()).isEqualTo(ULTIMATE_CREDITOR);
        assertThat(result.getPurposeCode()).isEqualTo(PURPOSE_CODE);
        assertThat(result.getRemittanceInformationStructured()).isEqualTo(remittanceMapper.mapToRemittanceInformationStructured(REMITTANCE));
    }

    @Test
    void mapToGetPaymentResponse12_Periodic_success() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(STANDARD_PAYMENT_TYPE)).thenReturn(false);
        when(amountModelMapper.mapToAmount(buildXs2aAmount())).thenReturn(getAmount12(true, true));

        //When
        PeriodicPaymentInitiationWithStatusResponse result = (PeriodicPaymentInitiationWithStatusResponse) paymentModelMapperPsd2.mapToGetPaymentResponse(buildPeriodicPayment(TransactionStatus.RCVD), PERIODIC, STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getEndToEndIdentification()).isEqualTo(END_TO_END_IDENTIFICATION);
        assertThat(result.getInstructionIdentification()).isEqualTo(INSTRUCTION_IDENTIFICATION);
        assertThat(result.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getInstructedAmount()).isEqualTo(getAmount12(true, true));
        assertThat(result.getCreditorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getCreditorAgent()).isEqualTo(CREDITOR_AGENT);
        assertThat(result.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(result.getCreditorAddress()).isEqualTo(getAddress12(true, true, true, true, true));
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION_UNSTRUCTED);
        assertThat(result.getStartDate()).isEqualTo(START_DATE);
        assertThat(result.getEndDate()).isEqualTo(END_DATE);
        assertThat(result.getExecutionRule()).isEqualTo(PSD2_EXECUTION_RULE);
        assertThat(result.getFrequency()).isEqualTo(PSD2_FREQUENCY_CODE);
        assertThat(result.getDayOfExecution()).isEqualTo(PSD2_DAY_OF_EXECUTION);
        assertThat(result.getTransactionStatus()).isEqualTo(de.adorsys.psd2.model.TransactionStatus.RCVD);
        assertThat(result.getUltimateDebtor()).isEqualTo(ULTIMATE_DEBTOR);
        assertThat(result.getUltimateCreditor()).isEqualTo(ULTIMATE_CREDITOR);
        assertThat(result.getPurposeCode()).isEqualTo(PURPOSE_CODE);
        assertThat(result.getRemittanceInformationStructured()).isEqualTo(remittanceMapper.mapToRemittanceInformationStructured(REMITTANCE));
    }

    @Test
    void mapToGetPaymentResponse12_Bulk_success() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(STANDARD_PAYMENT_TYPE)).thenReturn(false);
        when(amountModelMapper.mapToAmount(buildXs2aAmount())).thenReturn(getAmount12(true, true));

        //When
        BulkPaymentInitiationWithStatusResponse result = (BulkPaymentInitiationWithStatusResponse) paymentModelMapperPsd2.mapToGetPaymentResponse(buildBulkPayment(TransactionStatus.RCVD), BULK, STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPayments()).isNotNull();
        assertThat(result.getPayments()).isNotEmpty();
        assertThat(result.getBatchBookingPreferred()).isEqualTo(BATCH_BOOKING_PREFERRED);
        assertThat(result.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getRequestedExecutionDate()).isEqualTo(REQUESTED_EXECUTION_DATE);
        assertThat(result.getTransactionStatus()).isEqualTo(de.adorsys.psd2.model.TransactionStatus.RCVD);

        PaymentInitiationBulkElementJson bulkPaymentPart = result.getPayments().get(0);
        assertThat(bulkPaymentPart.getInstructedAmount()).isEqualTo(getAmount12(true, true));
        assertThat(bulkPaymentPart.getCreditorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(bulkPaymentPart.getCreditorAgent()).isEqualTo(CREDITOR_AGENT);
        assertThat(bulkPaymentPart.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(bulkPaymentPart.getCreditorAddress()).isEqualTo(getAddress12(true, true, true, true, true));
        assertThat(bulkPaymentPart.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION_UNSTRUCTED);
        assertThat(bulkPaymentPart.getUltimateDebtor()).isEqualTo(ULTIMATE_DEBTOR);
        assertThat(bulkPaymentPart.getUltimateCreditor()).isEqualTo(ULTIMATE_CREDITOR);
        assertThat(bulkPaymentPart.getPurposeCode()).isEqualTo(PURPOSE_CODE);
        assertThat(bulkPaymentPart.getRemittanceInformationStructured()).isEqualTo(remittanceMapper.mapToRemittanceInformationStructured(REMITTANCE));
        assertThat(bulkPaymentPart.getInstructionIdentification()).isEqualTo(INSTRUCTION_IDENTIFICATION);
    }

    @Test
    void mapToGetPaymentResponse12_NonStandardFormat_success() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(NON_STANDARD_PAYMENT_TYPE)).thenReturn(true);

        //When
        String result = (String) paymentModelMapperPsd2.mapToGetPaymentResponse(buildNonStandardPayment(), any(), NON_STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(NON_STANDARD_PAYMENT_DATA_STRING);
    }

    private Address getAddress12(boolean code, boolean str, boolean bld, boolean city, boolean country) {
        Address address = new Address();
        address.setPostCode(code ? "PostalCode" : null);
        address.setTownName(city ? "Kiev" : null);
        address.setBuildingNumber(bld ? "8" : null);
        address.setStreetName(str ? "Esplanadnaya" : null);
        address.setCountry(country ? "Ukraine" : null);
        return address;
    }

    private Amount getAmount12(boolean currency, boolean toPay) {
        de.adorsys.psd2.model.Amount instructedAmount = new de.adorsys.psd2.model.Amount();
        instructedAmount.setCurrency(currency ? "EUR" : null);
        instructedAmount.setAmount(toPay ? "123456" : null);
        return instructedAmount;
    }

    private de.adorsys.psd2.model.AccountReference getPsd2AccountReference(boolean iban, boolean currency) {
        de.adorsys.psd2.model.AccountReference accountReference = new de.adorsys.psd2.model.AccountReference();
        accountReference.setIban(iban ? IBAN : null);
        accountReference.setCurrency(currency ? CURRENCY : null);
        return accountReference;
    }

    private AccountReference getAccountReference(boolean iban, boolean currency) {
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(iban ? IBAN : null);
        accountReference.setCurrency(currency ? Currency.getInstance(CURRENCY) : null);
        return accountReference;
    }

    private Xs2aAmount buildXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(Currency.getInstance("EUR"));
        amount.setAmount("123456");
        return amount;
    }

    private CommonPayment buildNonStandardPayment() {
        CommonPayment commonPayment = new CommonPayment();
        commonPayment.setPaymentData(NON_STANDARD_PAYMENT_DATA_STRING.getBytes());
        return commonPayment;
    }

    private Xs2aAddress buildXs2aAddress() {
        Xs2aAddress address = new Xs2aAddress();
        address.setCountry(new Xs2aCountryCode("Ukraine"));
        address.setTownName("Kiev");
        address.setPostCode("PostalCode");
        address.setStreetName("Esplanadnaya");
        address.setBuildingNumber("8");
        return address;
    }

    private SinglePayment buildSinglePayment(TransactionStatus status) {
        SinglePayment payment = new SinglePayment();
        payment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        payment.setInstructionIdentification(INSTRUCTION_IDENTIFICATION);
        payment.setDebtorAccount(getAccountReference(true, true));
        payment.setInstructedAmount(buildXs2aAmount());
        payment.setCreditorAccount(getAccountReference(true, true));
        payment.setCreditorAgent(CREDITOR_AGENT);
        payment.setCreditorName(CREDITOR_NAME);
        payment.setCreditorAddress(buildXs2aAddress());
        payment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTED);
        payment.setTransactionStatus(status);
        payment.setUltimateDebtor(ULTIMATE_DEBTOR);
        payment.setUltimateCreditor(ULTIMATE_CREDITOR);
        payment.setPurposeCode(purposeCodeMapper.mapToPurposeCode(PURPOSE_CODE));
        payment.setRemittanceInformationStructured(REMITTANCE);
        return payment;
    }

    private PeriodicPayment buildPeriodicPayment(TransactionStatus status) {
        PeriodicPayment payment = new PeriodicPayment();
        payment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        payment.setInstructionIdentification(INSTRUCTION_IDENTIFICATION);
        payment.setDebtorAccount(getAccountReference(true, true));
        payment.setInstructedAmount(buildXs2aAmount());
        payment.setCreditorAccount(getAccountReference(true, true));
        payment.setCreditorAgent(CREDITOR_AGENT);
        payment.setCreditorName(CREDITOR_NAME);
        payment.setCreditorAddress(buildXs2aAddress());
        payment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTED);
        payment.setStartDate(START_DATE);
        payment.setEndDate(END_DATE);
        payment.setExecutionRule(XS2A_EXECUTION_RULE);
        payment.setFrequency(XS2A_FREQUENCY_CODE);
        payment.setDayOfExecution(XS2A_DAY_OF_EXECUTION);
        payment.setTransactionStatus(status);
        payment.setUltimateDebtor(ULTIMATE_DEBTOR);
        payment.setUltimateCreditor(ULTIMATE_CREDITOR);
        payment.setPurposeCode(purposeCodeMapper.mapToPurposeCode(PURPOSE_CODE));
        payment.setRemittanceInformationStructured(REMITTANCE);
        return payment;
    }

    private BulkPayment buildBulkPayment(TransactionStatus status) {
        BulkPayment payment = new BulkPayment();
        payment.setBatchBookingPreferred(BATCH_BOOKING_PREFERRED);
        payment.setDebtorAccount(getAccountReference(true, true));
        payment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        payment.setPayments(Collections.singletonList(buildSinglePayment(status)));
        payment.setTransactionStatus(status);
        return payment;
    }
}
