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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aPisCommonPaymentService {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    private final RequestProviderService requestProviderService;
    private final Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    private final Xs2aAuthenticationObjectToCmsScaMethodMapper xs2AAuthenticationObjectToCmsScaMethodMapper;


    public CreatePisCommonPaymentResponse createCommonPayment(PisPaymentInfo request) {
        return pisCommonPaymentServiceEncrypted.createCommonPayment(request)
                   .orElseGet(() -> {
                       log.info("InR-ID: [{}], X-Request-ID: [{}], Payment ID: [{}]. Pis common payment cannot be created, because can't save to cms DB",
                                requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getPaymentId());
                       return null;
                   });
    }

    public Optional<PisCommonPaymentResponse> getPisCommonPaymentById(String paymentId) {
        return pisCommonPaymentServiceEncrypted.getCommonPaymentById(paymentId);
    }

    public void updateSinglePaymentInCommonPayment(SinglePayment singlePayment, PaymentInitiationParameters paymentInitiationParameters, String paymentId) {
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsSinglePisCommonPaymentRequest(singlePayment, paymentInitiationParameters.getPaymentProduct());
        pisCommonPaymentServiceEncrypted.updateCommonPayment(pisCommonPaymentRequest, paymentId);
    }

    public void updatePeriodicPaymentInCommonPayment(PeriodicPayment periodicPayment, PaymentInitiationParameters paymentInitiationParameters, String paymentId) {
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsPeriodicPisCommonPaymentRequest(periodicPayment, paymentInitiationParameters.getPaymentProduct());
        pisCommonPaymentServiceEncrypted.updateCommonPayment(pisCommonPaymentRequest, paymentId);
    }

    public void updateBulkPaymentInCommonPayment(BulkPayment bulkPayment, PaymentInitiationParameters paymentInitiationParameters, String paymentId) {
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsBulkPisCommonPaymentRequest(bulkPayment, paymentInitiationParameters.getPaymentProduct());
        pisCommonPaymentServiceEncrypted.updateCommonPayment(pisCommonPaymentRequest, paymentId);
    }

    public Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(UpdatePisCommonPaymentPsuDataRequest request) {
        String authorisationId = request.getAuthorizationId();
        return pisCommonPaymentServiceEncrypted.updatePisAuthorisation(authorisationId, request);
    }

    public Optional<Boolean> updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        return pisCommonPaymentServiceEncrypted.updatePisAuthorisationStatus(authorisationId, scaStatus);
    }

    /**
     * Requests CMS to retrieve authentication method and checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return pisCommonPaymentServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    public boolean saveAuthenticationMethods(String authorisationId, List<Xs2aAuthenticationObject> methods) {
        return pisCommonPaymentServiceEncrypted.saveAuthenticationMethods(authorisationId, xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(methods));
    }

    /**
     * Updates PIS SCA approach in authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     sca approach
     */
    public void updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        pisCommonPaymentServiceEncrypted.updateScaApproach(authorisationId, scaApproach);
    }

    /**
     * Updates multilevelScaRequired and stores changes into database
     *
     * @param paymentId             Payment ID
     * @param multilevelScaRequired new value for boolean multilevel sca required
     */
    public boolean updateMultilevelSca(String paymentId, boolean multilevelScaRequired) {
        return pisCommonPaymentServiceEncrypted.updateMultilevelSca(paymentId, multilevelScaRequired);
    }
}
