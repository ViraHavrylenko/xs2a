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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OneOffConsentExpirationService {

    public static final int READ_ONLY_ACCOUNT_DETAILS_COUNT = 1;
    public static final int READ_ACCOUNT_DETAILS_AND_BALANCES_COUNT = 2;
    public static final int READ_ACCOUNT_DETAILS_AND_TRANSACTIONS_COUNT = 2;
    public static final int READ_ALL_DETAILS_COUNT = 3;

    private final AisConsentUsageRepository aisConsentUsageRepository;
    private final AisConsentTransactionRepository aisConsentTransactionRepository;
    private final ConsentDataMapper consentDataMapper;

    /**
     * Checks, should the one-off consent be expired after using its all GET endpoints (accounts, balances, transactions)
     * in all possible combinations depending on the consent type.
     *
     * @param consentId  consentId to check.
     * @param cmsConsent the {@link CmsConsent} to check.
     * @return true if the consent should be expired, false otherwise.
     */
    public boolean isConsentExpired(CmsConsent cmsConsent, Long consentId) {
        byte[] consentData = cmsConsent.getConsentData();
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(consentData);
        // ToDo fix consentRequestType resolution https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
        AisConsentRequestType consentRequestType = AisConsentRequestType.DEDICATED_ACCOUNTS;

        // We omit all bank offered consents until they are not populated with accounts.
        if (consentRequestType == AisConsentRequestType.BANK_OFFERED) {
            return false;
        }

        // All available account consent support only one call - readAccountList.
        if (consentRequestType == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS) {
            return true;
        }

        AccountAccess aspspAccountAccesses = cmsConsent.getAspspAccountAccesses();
        List<AccountReference> references = Stream.of(aspspAccountAccesses.getAccounts(), aspspAccountAccesses.getBalances(), aspspAccountAccesses.getTransactions())
                                                .flatMap(Collection::stream).collect(Collectors.toList());

        List<String> consentResourceIds = references.stream()
                                              .map(AccountReference::getResourceId)
                                              .distinct()
                                              .collect(Collectors.toList());

        boolean isExpired = true;
        for (String resourceId : consentResourceIds) {
            List<AisConsentTransaction> consentTransactions = aisConsentTransactionRepository.findByConsentIdAndResourceId(consentId,
                                                                                                                           resourceId,
                                                                                                                           PageRequest.of(0, 1));

            int numberOfTransactions = CollectionUtils.isNotEmpty(consentTransactions) ? consentTransactions.get(0).getNumberOfTransactions() : 0;

            int maximumNumberOfGetRequestsForConsent = getMaximumNumberOfGetRequestsForConsentsAccount(aspspAccountAccesses, resourceId, numberOfTransactions);
            int numberOfUsedGetRequestsForConsent = aisConsentUsageRepository.countByConsentIdAndResourceId(consentId, resourceId);

            // There are some available not used get requests - omit all other iterations.
            if (numberOfUsedGetRequestsForConsent < maximumNumberOfGetRequestsForConsent) {
                isExpired = false;
                break;
            }
        }

        return isExpired;
    }

    /**
     * This method returns maximum number of possible get requests for the definite consent for ONE account
     * except the main get call - readAccountList.
     */
    private int getMaximumNumberOfGetRequestsForConsentsAccount(AccountAccess aspspAccountAccesses, String resourceId, int numberOfTransactions) {

        boolean accessesForAccountsEmpty = isAccessForAccountReferencesEmpty(aspspAccountAccesses.getAccounts(), resourceId);
        boolean accessesForBalanceEmpty = isAccessForAccountReferencesEmpty(aspspAccountAccesses.getBalances(), resourceId);
        boolean accessesForTransactionsEmpty = isAccessForAccountReferencesEmpty(aspspAccountAccesses.getTransactions(), resourceId);

        // Consent was given only for accounts: readAccountDetails for each account.
        if (!accessesForAccountsEmpty
                && accessesForBalanceEmpty
                && accessesForTransactionsEmpty) {
            return READ_ONLY_ACCOUNT_DETAILS_COUNT;
        }

        // Consent was given for accounts and balances.
        if (accessesForTransactionsEmpty) {
            // Value 2 corresponds to the readAccountDetails and readBalances.
            return READ_ACCOUNT_DETAILS_AND_BALANCES_COUNT;
        }

        // Consent was given for accounts and transactions.
        if (accessesForBalanceEmpty) {
            // Value 2 corresponds to the readAccountDetails and readTransactions. Plus each account's transactions.
            return READ_ACCOUNT_DETAILS_AND_TRANSACTIONS_COUNT + numberOfTransactions;
        }

        // Consent was given for accounts, balances and transactions.
        return READ_ALL_DETAILS_COUNT + numberOfTransactions;
    }

    private boolean isAccessForAccountReferencesEmpty(List<AccountReference> accounts, String resourceId) {
        return accounts.stream().noneMatch(access -> access.getResourceId().equals(resourceId));
    }
}
