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

package de.adorsys.psd2.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CmsCommonPaymentMapperSupportImpl implements CmsCommonPaymentMapper {
    @Autowired
    protected Xs2aObjectMapper xs2aObjectMapper;

    @Override
    public CmsPayment mapToCmsSinglePayment(CmsCommonPayment cmsCommonPayment) {
        PaymentInitiationJson payment = convert(cmsCommonPayment.getPaymentData(), PaymentInitiationJson.class);
        if (payment == null) {
            return null;
        }
        return mapToCmsSinglePayment(payment, cmsCommonPayment);
    }

    @Override
    public CmsPayment mapToCmsBulkPayment(CmsCommonPayment cmsCommonPayment) {
        BulkPaymentInitiationJson payment = convert(cmsCommonPayment.getPaymentData(), BulkPaymentInitiationJson.class);
        if (payment == null) {
            return null;
        }
        return mapToCmsBulkPayment(payment, cmsCommonPayment);
    }

    @Override
    public CmsPayment mapToCmsPeriodicPayment(CmsCommonPayment cmsCommonPayment) {
        PeriodicPaymentInitiationJson payment = convert(cmsCommonPayment.getPaymentData(), PeriodicPaymentInitiationJson.class);
        if (payment == null) {
            return null;
        }

        return mapToCmsPeriodicPayment(payment, cmsCommonPayment);
    }

    private CmsPeriodicPayment mapToCmsPeriodicPayment(PeriodicPaymentInitiationJson periodicPaymentInitiationJson, CmsCommonPayment cmsCommonPayment) {

        CmsPeriodicPayment periodicPayment = new CmsPeriodicPayment(cmsCommonPayment.getPaymentProduct());

        periodicPayment.setEndToEndIdentification(periodicPaymentInitiationJson.getEndToEndIdentification());
        periodicPayment.setDebtorAccount(mapToCmsAccountReference(periodicPaymentInitiationJson.getDebtorAccount()));
        Amount instructedAmount = periodicPaymentInitiationJson.getInstructedAmount();
        periodicPayment.setInstructedAmount(new CmsAmount(Currency.getInstance(instructedAmount.getCurrency()), BigDecimal.valueOf(Double.parseDouble(instructedAmount.getAmount()))));
        periodicPayment.setCreditorAccount(mapToCmsAccountReference(periodicPaymentInitiationJson.getCreditorAccount()));
        periodicPayment.setCreditorAgent(periodicPaymentInitiationJson.getCreditorAgent());
        periodicPayment.setCreditorName(periodicPaymentInitiationJson.getCreditorName());
        periodicPayment.setCreditorAddress(mapToCmsAddress(periodicPaymentInitiationJson.getCreditorAddress()));
        periodicPayment.setRemittanceInformationUnstructured(periodicPaymentInitiationJson.getRemittanceInformationUnstructured());
        periodicPayment.setDayOfExecution(mapToPisDayOfExecution(periodicPaymentInitiationJson.getDayOfExecution()));
        periodicPayment.setStartDate(periodicPaymentInitiationJson.getStartDate());
        periodicPayment.setEndDate(periodicPaymentInitiationJson.getEndDate());
        periodicPayment.setExecutionRule(mapToPisExecutionRule(periodicPaymentInitiationJson.getExecutionRule()));
        periodicPayment.setFrequency(mapToPisDayOfExecution(periodicPaymentInitiationJson.getFrequency()));
        periodicPayment.setUltimateDebtor(periodicPaymentInitiationJson.getUltimateDebtor());
        periodicPayment.setUltimateCreditor(periodicPaymentInitiationJson.getUltimateCreditor());
        periodicPayment.setPurposeCode(mapToPurposeCode(periodicPaymentInitiationJson.getPurposeCode()));
        periodicPayment.setRemittanceInformationStructured(mapToCmsRemittance(periodicPaymentInitiationJson.getRemittanceInformationStructured()));

        return periodicPayment;
    }

    private CmsBulkPayment mapToCmsBulkPayment(BulkPaymentInitiationJson bulkPaymentInitiationJson, CmsCommonPayment cmsCommonPayment) {

        CmsBulkPayment bulkPayment = new CmsBulkPayment();
        fillBasePaymentFields(bulkPayment, cmsCommonPayment);
        bulkPayment.setBatchBookingPreferred(bulkPaymentInitiationJson.getBatchBookingPreferred());
        bulkPayment.setDebtorAccount(mapToCmsAccountReference(bulkPaymentInitiationJson.getDebtorAccount()));
        bulkPayment.setBatchBookingPreferred(bulkPaymentInitiationJson.getBatchBookingPreferred());
        bulkPayment.setRequestedExecutionDate(bulkPaymentInitiationJson.getRequestedExecutionDate());
        List<CmsSinglePayment> payments = bulkPaymentInitiationJson.getPayments().stream()
                                              .map(p -> mapToCmsSinglePayment(p, cmsCommonPayment))
                                              .collect(Collectors.toList());
        bulkPayment.setPayments(payments);

        return bulkPayment;
    }

    private CmsSinglePayment mapToCmsSinglePayment(PaymentInitiationBulkElementJson paymentInitiationBulkElementJson, CmsCommonPayment cmsCommonPayment) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(cmsCommonPayment.getPaymentProduct());
        fillBasePaymentFields(singlePayment, cmsCommonPayment);
        singlePayment.setEndToEndIdentification(paymentInitiationBulkElementJson.getEndToEndIdentification());
        Amount instructedAmount = paymentInitiationBulkElementJson.getInstructedAmount();
        singlePayment.setInstructedAmount(new CmsAmount(Currency.getInstance(instructedAmount.getCurrency()), BigDecimal.valueOf(Double.parseDouble(instructedAmount.getAmount()))));
        singlePayment.setCreditorAccount(mapToCmsAccountReference(paymentInitiationBulkElementJson.getCreditorAccount()));
        singlePayment.setCreditorAgent(paymentInitiationBulkElementJson.getCreditorAgent());
        singlePayment.setCreditorName(paymentInitiationBulkElementJson.getCreditorName());
        singlePayment.setCreditorAddress(mapToCmsAddress(paymentInitiationBulkElementJson.getCreditorAddress()));
        singlePayment.setRemittanceInformationUnstructured(paymentInitiationBulkElementJson.getRemittanceInformationUnstructured());
        singlePayment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());
        singlePayment.setUltimateDebtor(paymentInitiationBulkElementJson.getUltimateDebtor());
        singlePayment.setUltimateCreditor(paymentInitiationBulkElementJson.getUltimateCreditor());
        singlePayment.setPurposeCode(mapToPurposeCode(paymentInitiationBulkElementJson.getPurposeCode()));
        singlePayment.setRemittanceInformationStructured(mapToCmsRemittance(paymentInitiationBulkElementJson.getRemittanceInformationStructured()));
        return singlePayment;
    }

    private CmsSinglePayment mapToCmsSinglePayment(PaymentInitiationJson paymentInitiationJson, CmsCommonPayment cmsCommonPayment) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(cmsCommonPayment.getPaymentProduct());
        fillBasePaymentFields(singlePayment, cmsCommonPayment);
        singlePayment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
        singlePayment.setDebtorAccount(mapToCmsAccountReference(paymentInitiationJson.getDebtorAccount()));
        Amount instructedAmount = paymentInitiationJson.getInstructedAmount();
        singlePayment.setInstructedAmount(new CmsAmount(Currency.getInstance(instructedAmount.getCurrency()), BigDecimal.valueOf(Double.parseDouble(instructedAmount.getAmount()))));
        singlePayment.setCreditorAccount(mapToCmsAccountReference(paymentInitiationJson.getCreditorAccount()));
        singlePayment.setCreditorAgent(paymentInitiationJson.getCreditorAgent());
        singlePayment.setCreditorName(paymentInitiationJson.getCreditorName());
        singlePayment.setCreditorAddress(mapToCmsAddress(paymentInitiationJson.getCreditorAddress()));
        singlePayment.setRemittanceInformationUnstructured(paymentInitiationJson.getRemittanceInformationUnstructured());
        singlePayment.setRequestedExecutionDate(paymentInitiationJson.getRequestedExecutionDate());
        singlePayment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());
        singlePayment.setUltimateDebtor(paymentInitiationJson.getUltimateDebtor());
        singlePayment.setUltimateCreditor(paymentInitiationJson.getUltimateCreditor());
        singlePayment.setPurposeCode(mapToPurposeCode(paymentInitiationJson.getPurposeCode()));
        singlePayment.setRemittanceInformationStructured(mapToCmsRemittance(paymentInitiationJson.getRemittanceInformationStructured()));
        return singlePayment;
    }

    private <T> T convert(byte[] paymentData, Class<T> tClass) {
        try {
            return xs2aObjectMapper.readValue(paymentData, tClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fillBasePaymentFields(BaseCmsPayment payment, CmsCommonPayment cmsCommonPayment) {
        payment.setPaymentProduct(cmsCommonPayment.getPaymentProduct());
        payment.setPaymentId(cmsCommonPayment.getPaymentId());
        payment.setTppInfo(cmsCommonPayment.getTppInfo());
        payment.setPsuIdDatas(cmsCommonPayment.getPsuIdDatas());
        payment.setCreationTimestamp(cmsCommonPayment.getCreationTimestamp());
        payment.setStatusChangeTimestamp(cmsCommonPayment.getStatusChangeTimestamp());
    }

    private CmsRemittance mapToCmsRemittance(RemittanceInformationStructured remittanceInformationStructured) {
        if (remittanceInformationStructured == null) {
            return null;
        }

        CmsRemittance cmsRemittance = new CmsRemittance();
        cmsRemittance.setReference(remittanceInformationStructured.getReference());
        cmsRemittance.setReferenceType(remittanceInformationStructured.getReferenceType());
        cmsRemittance.setReferenceIssuer(remittanceInformationStructured.getReferenceIssuer());

        return cmsRemittance;
    }

    private CmsAddress mapToCmsAddress(Address pisAddress) {
        return Optional.ofNullable(pisAddress)
                   .map(adr -> {
                       CmsAddress cmsAddress = new CmsAddress();
                       cmsAddress.setStreet(adr.getStreetName());
                       cmsAddress.setBuildingNumber(adr.getBuildingNumber());
                       cmsAddress.setCity(adr.getTownName());
                       cmsAddress.setPostalCode(adr.getPostCode());
                       cmsAddress.setCountry(adr.getCountry());
                       return cmsAddress;
                   }).orElse(null);
    }

    private CmsAccountReference mapToCmsAccountReference(AccountReference pisAccountReference) {
        return Optional.ofNullable(pisAccountReference)
                   .map(ref -> new CmsAccountReference(null,
                                                       ref.getIban(),
                                                       ref.getBban(),
                                                       ref.getPan(),
                                                       ref.getMaskedPan(),
                                                       ref.getMsisdn(),
                                                       Currency.getInstance(ref.getCurrency()))
                   ).orElse(null);
    }

    private FrequencyCode mapToPisDayOfExecution(de.adorsys.psd2.model.FrequencyCode frequencyCode) {
        return Optional.ofNullable(frequencyCode).map(de.adorsys.psd2.model.FrequencyCode::toString).map(FrequencyCode::valueOf).orElse(null);
    }

    private PisDayOfExecution mapToPisDayOfExecution(DayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution).map(DayOfExecution::toString).map(PisDayOfExecution::fromValue).orElse(null);
    }

    private PisExecutionRule mapToPisExecutionRule(ExecutionRule executionRule) {
        return Optional.ofNullable(executionRule).map(ExecutionRule::toString).map(PisExecutionRule::valueOf).orElse(null);
    }

    private String mapToPurposeCode(PurposeCode purposeCode) {
        return Optional.ofNullable(purposeCode).map(PurposeCode::toString).orElse(null);
    }
}
