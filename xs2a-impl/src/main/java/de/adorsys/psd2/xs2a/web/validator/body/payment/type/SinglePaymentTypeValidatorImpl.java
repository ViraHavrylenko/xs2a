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

package de.adorsys.psd2.xs2a.web.validator.body.payment.type;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.pis.Remittance;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Component
public class SinglePaymentTypeValidatorImpl extends AbstractBodyValidatorImpl implements PaymentTypeValidator {

    PaymentMapper paymentMapper;
    private AmountValidator amountValidator;
    protected PaymentValidationConfig validationConfig;

    @Autowired
    public SinglePaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                          PaymentMapper paymentMapper, AmountValidator amountValidator, PaymentValidationConfig validationConfig) {
        super(errorBuildingService, xs2aObjectMapper);
        this.paymentMapper = paymentMapper;
        this.amountValidator = amountValidator;
        this.validationConfig = validationConfig;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE;
    }

    @Override
    public MessageError validate(Object body, MessageError messageError) {
        try {
            doSingleValidation(paymentMapper.getSinglePayment(body), messageError);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unrecognized field")) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EXTRA_FIELD, extractErrorField(e.getMessage())));
            } else {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR));
            }
        }

        return messageError;
    }

    void doSingleValidation(SinglePayment singlePayment, MessageError messageError) {
        checkFieldForMaxLength(singlePayment.getEndToEndIdentification(), "endToEndIdentification", validationConfig.getEndToEndIdentification(), messageError);

        if (singlePayment.getDebtorAccount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "debtorAccount"));
        } else {
            validateAccount(singlePayment.getDebtorAccount(), messageError);
        }

        if (singlePayment.getInstructedAmount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "instructedAmount"));
        } else {
            validateInstructedAmount(singlePayment.getInstructedAmount(), messageError);
        }

        if (singlePayment.getCreditorAccount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "creditorAccount"));
        } else {
            validateAccount(singlePayment.getCreditorAccount(), messageError);
        }

        checkFieldForMaxLength(singlePayment.getCreditorName(), "creditorName", validationConfig.getCreditorName(), messageError);

        if (singlePayment.getCreditorAddress() != null) {
            validateAddress(singlePayment.getCreditorAddress(), messageError);
        }

        if (isDateInThePast(singlePayment.getRequestedExecutionDate())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(EXECUTION_DATE_INVALID_IN_THE_PAST));
        }

        checkFieldForMaxLength(singlePayment.getCreditorId(), "creditorId", validationConfig.getCreditorId(), messageError);
        checkFieldForMaxLength(singlePayment.getUltimateDebtor(), "ultimateDebtor", validationConfig.getUltimateDebtor(), messageError);
        checkFieldForMaxLength(singlePayment.getUltimateCreditor(), "ultimateCreditor", validationConfig.getUltimateDebtor(), messageError);
        validateRemittanceInformationStructured(singlePayment.getRemittanceInformationStructured(), messageError);
    }

    void validateAddress(Xs2aAddress address, MessageError messageError) {
        checkFieldForMaxLength(address.getStreetName(), "streetName", validationConfig.getStreetName(), messageError);
        checkFieldForMaxLength(address.getBuildingNumber(), "buildingNumber", validationConfig.getBuildingNumber(), messageError);
        checkFieldForMaxLength(address.getTownName(), "townName", validationConfig.getTownName(), messageError);
        checkFieldForMaxLength(address.getPostCode(), "postCode", validationConfig.getPostCode(), messageError);

        if (address.getCountry() == null || StringUtils.isBlank(address.getCountry().getCode())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_VALUE_REQUIRED, "address.country"));
        } else if (!Arrays.asList(Locale.getISOCountries()).contains(address.getCountry().getCode())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_ADDRESS_COUNTRY_INCORRECT));
        }
    }

    private void validateInstructedAmount(Xs2aAmount instructedAmount, MessageError messageError) {
        if (instructedAmount.getCurrency() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, "currency"));
        }
        if (instructedAmount.getAmount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "amount"));
        } else {
            amountValidator.validateAmount(instructedAmount.getAmount(), messageError);
        }
    }

    void validateAccount(AccountReference accountReference, MessageError messageError) {
        if (StringUtils.isNotBlank(accountReference.getIban()) && !isValidIban(accountReference.getIban())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, "IBAN"));
        }
        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, "BBAN"));
        }

        checkFieldForMaxLength(accountReference.getPan(), "PAN", validationConfig.getPan(), messageError);
        checkFieldForMaxLength(accountReference.getMaskedPan(), "Masked PAN", validationConfig.getMaskedPan(), messageError);
        checkFieldForMaxLength(accountReference.getMsisdn(), "MSISDN", validationConfig.getMsisdn(), messageError);
    }

    private boolean isValidIban(String iban) {
        IBANValidator validator = IBANValidator.getInstance();
        return validator.isValid(iban);
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }

    private void validateRemittanceInformationStructured(Remittance remittance, MessageError messageError) {
        if (remittance != null) {
            checkFieldForMaxLength(remittance.getReference(), "reference", validationConfig.getReference(), messageError);
            checkFieldForMaxLength(remittance.getReferenceType(), "referenceType", validationConfig.getReferenceType(), messageError);
            checkFieldForMaxLength(remittance.getReferenceIssuer(), "referenceIssuer", validationConfig.getReferenceIssuer(), messageError);
        }
    }

    boolean isDateInThePast(LocalDate dateToCheck) {
        return Optional.ofNullable(dateToCheck)
                   .map(date -> date.isBefore(LocalDate.now()))
                   .orElse(false);
    }
}
