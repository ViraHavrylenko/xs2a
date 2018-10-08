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

package de.adorsys.aspsp.aspspmockserver.domain.pis.authorisation;

import de.adorsys.aspsp.aspspmockserver.domain.AspspConsentData;
import de.adorsys.aspsp.aspspmockserver.domain.ScaStatus;
import lombok.Data;

@Data
public class UpdatePisConsentPsuDataRequest {
    private String paymentId;
    private String authorizationId;
    private String psuId;
    private String password;
    private String authenticationMethodId;
    private ScaStatus scaStatus;
    private String paymentService;
    private AspspConsentData aspspConsentData;
    private String scaAuthenticationData;
}
