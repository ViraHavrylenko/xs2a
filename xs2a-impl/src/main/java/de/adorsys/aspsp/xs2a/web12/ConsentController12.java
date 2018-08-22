/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.web12;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.api.ConsentApi;
import de.adorsys.psd2.model.Consents;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "AISP, Consents version 1.2", description = "Provides access to the Psu Consents")
public class ConsentController12 implements ConsentApi {
    private final ConsentService consentService;
    private final ResponseMapper responseMapper;
    private final AccountReferenceValidationService referenceValidationService;
    private final ConsentModelMapper consentModelMapper;

    @Override
    public ResponseEntity<?> createConsent(UUID xRequestID, Consents body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, Boolean tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        CreateConsentReq createConsent = consentModelMapper.mapToCreateConsentReq(body);

        Set<AccountReference> references = createConsent.getAccountReferences();
        ResponseObject accountReferenceValidationResponse = references.isEmpty()
                                                                ? ResponseObject.builder().build()
                                                                : referenceValidationService.validateAccountReferences(createConsent.getAccountReferences());

        ResponseObject<CreateConsentResponse> createConsentResponse = accountReferenceValidationResponse.hasError()
                                                                          ? ResponseObject.<CreateConsentResponse>builder().fail(accountReferenceValidationResponse.getError()).build()
                                                                          : consentService.createAccountConsentsWithResponse(createConsent, PSU_ID);

        return responseMapper.created(consentModelMapper.mapToConsentsResponse201ResponseObject(createConsentResponse));
    }

    @Override
    public ResponseEntity<?> getConsentStatus(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<ConsentStatusResponse> consentStatusResponse = consentService.getAccountConsentsStatusById(consentId);
        return responseMapper.ok(consentModelMapper.mapToConsentStatusResponse200ResponseObject(consentStatusResponse));
    }

    @Override
    public ResponseEntity<?> getConsentInformation(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<AccountConsent> response = consentService.getAccountConsentById(consentId);
        return responseMapper.ok(consentModelMapper.mapToConsentInformationResponse200JsonResponseObject(response));
    }

    @Override
    public ResponseEntity<?> deleteConsent(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Void> response = consentService.deleteAccountConsentsById(consentId);
        return responseMapper.delete(response);
    }
}
