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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Transaction details.
 */
@ApiModel(description = "Transaction details.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-02-28T17:40:20.531650+02:00[Europe/Kiev]")

public class TransactionDetails   {
  @JsonProperty("transactionId")
  private String transactionId = null;

  @JsonProperty("entryReference")
  private String entryReference = null;

  @JsonProperty("endToEndId")
  private String endToEndId = null;

  @JsonProperty("mandateId")
  private String mandateId = null;

  @JsonProperty("checkId")
  private String checkId = null;

  @JsonProperty("creditorId")
  private String creditorId = null;

  @JsonProperty("bookingDate")
  private LocalDate bookingDate = null;

  @JsonProperty("valueDate")
  private LocalDate valueDate = null;

  @JsonProperty("transactionAmount")
  private Amount transactionAmount = null;

  @JsonProperty("currencyExchange")
  private ReportExchangeRateList currencyExchange = null;

  @JsonProperty("creditorName")
  private String creditorName = null;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount = null;

  @JsonProperty("creditorAgent")
  private String creditorAgent = null;

  @JsonProperty("ultimateCreditor")
  private String ultimateCreditor = null;

  @JsonProperty("debtorName")
  private String debtorName = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

    @JsonProperty("debtorAgent")
    private String debtorAgent = null;

    @JsonProperty("ultimateDebtor")
    private String ultimateDebtor = null;

    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured = null;

    @JsonProperty("remittanceInformationUnstructuredArray")
    private RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray = null;

    @JsonProperty("remittanceInformationStructured")
    private String remittanceInformationStructured = null;

    @JsonProperty("remittanceInformationStructuredArray")
    @Valid
    private List<String> remittanceInformationStructuredArray = null;

    @JsonProperty("additionalInformation")
    private String additionalInformation = null;

    @JsonProperty("purposeCode")
    private PurposeCode purposeCode = null;

    @JsonProperty("bankTransactionCode")
    private String bankTransactionCode = null;

  @JsonProperty("proprietaryBankTransactionCode")
  private String proprietaryBankTransactionCode = null;

  @JsonProperty("additionalInformationStructured")
  private AdditionalInformationStructured additionalInformationStructured = null;

  @JsonProperty("balanceAfterTransaction")
  private Balance balanceAfterTransaction = null;

  @JsonProperty("_links")
  private Map _links = null;

  public TransactionDetails transactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

    /**
     * the Transaction Id can be used as access-ID in the API, where more details on an transaction is offered. If this data attribute is provided this shows that the AIS can get access on more details about this transaction using the Get transaction details request.
     *
     * @return transactionId
     **/
    @ApiModelProperty(value = "the Transaction Id can be used as access-ID in the API, where more details on an transaction is offered. If this data attribute is provided this shows that the AIS can get access on more details about this transaction using the Get transaction details request. ")



  @JsonProperty("transactionId")
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public TransactionDetails entryReference(String entryReference) {
    this.entryReference = entryReference;
    return this;
  }

  /**
   * Get entryReference
   * @return entryReference
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("entryReference")
  public String getEntryReference() {
    return entryReference;
  }

  public void setEntryReference(String entryReference) {
    this.entryReference = entryReference;
  }

  public TransactionDetails endToEndId(String endToEndId) {
    this.endToEndId = endToEndId;
    return this;
  }

  /**
   * Unique end to end identity.
   * @return endToEndId
  **/
  @ApiModelProperty(value = "Unique end to end identity.")

@Size(max=35)

  @JsonProperty("endToEndId")
  public String getEndToEndId() {
    return endToEndId;
  }

  public void setEndToEndId(String endToEndId) {
    this.endToEndId = endToEndId;
  }

  public TransactionDetails mandateId(String mandateId) {
    this.mandateId = mandateId;
    return this;
  }

  /**
   * Identification of Mandates, e.g. a SEPA Mandate ID.
   * @return mandateId
  **/
  @ApiModelProperty(value = "Identification of Mandates, e.g. a SEPA Mandate ID.")

@Size(max=35)

  @JsonProperty("mandateId")
  public String getMandateId() {
    return mandateId;
  }

  public void setMandateId(String mandateId) {
    this.mandateId = mandateId;
  }

  public TransactionDetails checkId(String checkId) {
    this.checkId = checkId;
    return this;
  }

  /**
   * Identification of a Cheque.
   * @return checkId
  **/
  @ApiModelProperty(value = "Identification of a Cheque.")

@Size(max=35)

  @JsonProperty("checkId")
  public String getCheckId() {
    return checkId;
  }

  public void setCheckId(String checkId) {
    this.checkId = checkId;
  }

  public TransactionDetails creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Identification of Creditors, e.g. a SEPA Creditor ID.
   * @return creditorId
  **/
  @ApiModelProperty(value = "Identification of Creditors, e.g. a SEPA Creditor ID.")

@Size(max=35)

  @JsonProperty("creditorId")
  public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public TransactionDetails bookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
    return this;
  }

  /**
   * Get bookingDate
   * @return bookingDate
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("bookingDate")
  public LocalDate getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
  }

  public TransactionDetails valueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
    return this;
  }

  /**
   * The Date at which assets become available to the account owner in case of a credit.
   * @return valueDate
  **/
  @ApiModelProperty(value = "The Date at which assets become available to the account owner in case of a credit.")

  @Valid


  @JsonProperty("valueDate")
  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }

  public TransactionDetails transactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
    return this;
  }

  /**
   * Get transactionAmount
   * @return transactionAmount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("transactionAmount")
  public Amount getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public TransactionDetails currencyExchange(ReportExchangeRateList currencyExchange) {
    this.currencyExchange = currencyExchange;
    return this;
  }

  /**
   * Get currencyExchange
   * @return currencyExchange
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("currencyExchange")
  public ReportExchangeRateList getCurrencyExchange() {
    return currencyExchange;
  }

  public void setCurrencyExchange(ReportExchangeRateList currencyExchange) {
    this.currencyExchange = currencyExchange;
  }

  public TransactionDetails creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Get creditorName
   * @return creditorName
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("creditorName")
  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public TransactionDetails creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("creditorAccount")
  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public TransactionDetails creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * Get creditorAgent
   * @return creditorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("creditorAgent")
  public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public TransactionDetails ultimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
    return this;
  }

  /**
   * Get ultimateCreditor
   * @return ultimateCreditor
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("ultimateCreditor")
  public String getUltimateCreditor() {
    return ultimateCreditor;
  }

  public void setUltimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
  }

  public TransactionDetails debtorName(String debtorName) {
    this.debtorName = debtorName;
    return this;
  }

  /**
   * Get debtorName
   * @return debtorName
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("debtorName")
  public String getDebtorName() {
    return debtorName;
  }

  public void setDebtorName(String debtorName) {
    this.debtorName = debtorName;
  }

  public TransactionDetails debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("debtorAccount")
  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public TransactionDetails debtorAgent(String debtorAgent) {
    this.debtorAgent = debtorAgent;
    return this;
  }

  /**
   * Get debtorAgent
   * @return debtorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("debtorAgent")
  public String getDebtorAgent() {
    return debtorAgent;
  }

  public void setDebtorAgent(String debtorAgent) {
    this.debtorAgent = debtorAgent;
  }

  public TransactionDetails ultimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
    return this;
  }

  /**
   * Get ultimateDebtor
   * @return ultimateDebtor
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("ultimateDebtor")
  public String getUltimateDebtor() {
    return ultimateDebtor;
  }

  public void setUltimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
  }

  public TransactionDetails remittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    return this;
  }

  /**
   * Get remittanceInformationUnstructured
   * @return remittanceInformationUnstructured
  **/
  @ApiModelProperty(value = "")

@Size(max = 140)

  @JsonProperty("remittanceInformationUnstructured")
  public String getRemittanceInformationUnstructured() {
      return remittanceInformationUnstructured;
  }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public TransactionDetails remittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
        this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
        return this;
    }

    /**
     * Get remittanceInformationUnstructuredArray
     *
     * @return remittanceInformationUnstructuredArray
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("remittanceInformationUnstructuredArray")
    public RemittanceInformationUnstructuredArray getRemittanceInformationUnstructuredArray() {
        return remittanceInformationUnstructuredArray;
    }

    public void setRemittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
        this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
    }

    public TransactionDetails remittanceInformationStructured(String remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
        return this;
    }

    /**
     * Reference as contained in the structured remittance reference structure (without the surrounding XML structure).  Different from other places the content is containt in plain form not in form of a structered field.
     * @return remittanceInformationStructured
  **/
  @ApiModelProperty(value = "Reference as contained in the structured remittance reference structure (without the surrounding XML structure).  Different from other places the content is containt in plain form not in form of a structered field. ")

  @Size(max = 140)

  @JsonProperty("remittanceInformationStructured")
  public String getRemittanceInformationStructured() {
      return remittanceInformationStructured;
  }

    public void setRemittanceInformationStructured(String remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
    }

    public TransactionDetails remittanceInformationStructuredArray(List<String> remittanceInformationStructuredArray) {
        this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
        return this;
    }

    public TransactionDetails addRemittanceInformationStructuredArrayItem(String remittanceInformationStructuredArrayItem) {
        if (this.remittanceInformationStructuredArray == null) {
            this.remittanceInformationStructuredArray = new ArrayList<>();
        }
        this.remittanceInformationStructuredArray.add(remittanceInformationStructuredArrayItem);
        return this;
    }

    /**
     * Get remittanceInformationStructuredArray
     *
     * @return remittanceInformationStructuredArray
     **/
    @ApiModelProperty(value = "")


    @JsonProperty("remittanceInformationStructuredArray")
    public List<String> getRemittanceInformationStructuredArray() {
        return remittanceInformationStructuredArray;
    }

    public void setRemittanceInformationStructuredArray(List<String> remittanceInformationStructuredArray) {
        this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
    }

    public TransactionDetails additionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }

    /**
     * Might be used by the ASPSP to transport additional transaction related information to the PSU.
     * @return additionalInformation
  **/
  @ApiModelProperty(value = "Might be used by the ASPSP to transport additional transaction related information to the PSU. ")

@Size(max=500)

  @JsonProperty("additionalInformation")
  public String getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  public TransactionDetails purposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
    return this;
  }

  /**
   * Get purposeCode
   * @return purposeCode
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("purposeCode")
  public PurposeCode getPurposeCode() {
    return purposeCode;
  }

  public void setPurposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
  }

  public TransactionDetails bankTransactionCode(String bankTransactionCode) {
    this.bankTransactionCode = bankTransactionCode;
    return this;
  }

  /**
   * Get bankTransactionCode
   * @return bankTransactionCode
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("bankTransactionCode")
  public String getBankTransactionCode() {
    return bankTransactionCode;
  }

  public void setBankTransactionCode(String bankTransactionCode) {
    this.bankTransactionCode = bankTransactionCode;
  }

  public TransactionDetails proprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    return this;
  }

  /**
   * Get proprietaryBankTransactionCode
   * @return proprietaryBankTransactionCode
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("proprietaryBankTransactionCode")
  public String getProprietaryBankTransactionCode() {
    return proprietaryBankTransactionCode;
  }

  public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
  }

  public TransactionDetails additionalInformationStructured(AdditionalInformationStructured additionalInformationStructured) {
    this.additionalInformationStructured = additionalInformationStructured;
    return this;
  }

  /**
   * Get additionalInformationStructured
   * @return additionalInformationStructured
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("additionalInformationStructured")
  public AdditionalInformationStructured getAdditionalInformationStructured() {
    return additionalInformationStructured;
  }

  public void setAdditionalInformationStructured(AdditionalInformationStructured additionalInformationStructured) {
    this.additionalInformationStructured = additionalInformationStructured;
  }

  public TransactionDetails balanceAfterTransaction(Balance balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
    return this;
  }

  /**
   * Get balanceAfterTransaction
   * @return balanceAfterTransaction
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("balanceAfterTransaction")
  public Balance getBalanceAfterTransaction() {
    return balanceAfterTransaction;
  }

  public void setBalanceAfterTransaction(Balance balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
  }

  public TransactionDetails _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    TransactionDetails transactionDetails = (TransactionDetails) o;
    return Objects.equals(this.transactionId, transactionDetails.transactionId) &&
    Objects.equals(this.entryReference, transactionDetails.entryReference) &&
    Objects.equals(this.endToEndId, transactionDetails.endToEndId) &&
    Objects.equals(this.mandateId, transactionDetails.mandateId) &&
    Objects.equals(this.checkId, transactionDetails.checkId) &&
    Objects.equals(this.creditorId, transactionDetails.creditorId) &&
    Objects.equals(this.bookingDate, transactionDetails.bookingDate) &&
    Objects.equals(this.valueDate, transactionDetails.valueDate) &&
    Objects.equals(this.transactionAmount, transactionDetails.transactionAmount) &&
               Objects.equals(this.currencyExchange, transactionDetails.currencyExchange) &&
               Objects.equals(this.creditorName, transactionDetails.creditorName) &&
               Objects.equals(this.creditorAccount, transactionDetails.creditorAccount) &&
               Objects.equals(this.creditorAgent, transactionDetails.creditorAgent) &&
               Objects.equals(this.ultimateCreditor, transactionDetails.ultimateCreditor) &&
               Objects.equals(this.debtorName, transactionDetails.debtorName) &&
               Objects.equals(this.debtorAccount, transactionDetails.debtorAccount) &&
               Objects.equals(this.debtorAgent, transactionDetails.debtorAgent) &&
               Objects.equals(this.ultimateDebtor, transactionDetails.ultimateDebtor) &&
               Objects.equals(this.remittanceInformationUnstructured, transactionDetails.remittanceInformationUnstructured) &&
               Objects.equals(this.remittanceInformationUnstructuredArray, transactionDetails.remittanceInformationUnstructuredArray) &&
               Objects.equals(this.remittanceInformationStructured, transactionDetails.remittanceInformationStructured) &&
               Objects.equals(this.remittanceInformationStructuredArray, transactionDetails.remittanceInformationStructuredArray) &&
               Objects.equals(this.additionalInformation, transactionDetails.additionalInformation) &&
               Objects.equals(this.purposeCode, transactionDetails.purposeCode) &&
               Objects.equals(this.bankTransactionCode, transactionDetails.bankTransactionCode) &&
               Objects.equals(this.proprietaryBankTransactionCode, transactionDetails.proprietaryBankTransactionCode) &&
               Objects.equals(this.additionalInformationStructured, transactionDetails.additionalInformationStructured) &&
               Objects.equals(this.balanceAfterTransaction, transactionDetails.balanceAfterTransaction) &&
               Objects.equals(this._links, transactionDetails._links);
  }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, entryReference, endToEndId, mandateId, checkId, creditorId, bookingDate, valueDate, transactionAmount, currencyExchange, creditorName, creditorAccount, creditorAgent, ultimateCreditor, debtorName, debtorAccount, debtorAgent, ultimateDebtor, remittanceInformationUnstructured, remittanceInformationUnstructuredArray, remittanceInformationStructured, remittanceInformationStructuredArray, additionalInformation, purposeCode, bankTransactionCode, proprietaryBankTransactionCode, additionalInformationStructured, balanceAfterTransaction, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionDetails {\n");

    sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
    sb.append("    entryReference: ").append(toIndentedString(entryReference)).append("\n");
    sb.append("    endToEndId: ").append(toIndentedString(endToEndId)).append("\n");
    sb.append("    mandateId: ").append(toIndentedString(mandateId)).append("\n");
    sb.append("    checkId: ").append(toIndentedString(checkId)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
    sb.append("    valueDate: ").append(toIndentedString(valueDate)).append("\n");
    sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
    sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    debtorAgent: ").append(toIndentedString(debtorAgent)).append("\n");
    sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
      sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
      sb.append("    remittanceInformationUnstructuredArray: ").append(toIndentedString(remittanceInformationUnstructuredArray)).append("\n");
      sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
      sb.append("    remittanceInformationStructuredArray: ").append(toIndentedString(remittanceInformationStructuredArray)).append("\n");
      sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
    sb.append("    bankTransactionCode: ").append(toIndentedString(bankTransactionCode)).append("\n");
    sb.append("    proprietaryBankTransactionCode: ").append(toIndentedString(proprietaryBankTransactionCode)).append("\n");
    sb.append("    additionalInformationStructured: ").append(toIndentedString(additionalInformationStructured)).append("\n");
    sb.append("    balanceAfterTransaction: ").append(toIndentedString(balanceAfterTransaction)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

