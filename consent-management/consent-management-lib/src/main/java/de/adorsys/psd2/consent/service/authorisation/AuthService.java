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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;

import java.util.List;
import java.util.Optional;

public interface AuthService {
    Optional<Authorisable> getNotFinalisedAuthorisationParent(String parentId);

    Optional<Authorisable> getAuthorisationParent(String parentId);

    AuthorisationEntity saveAuthorisation(CreateAuthorisationRequest request, Authorisable authorisationParent);

    AuthorisationEntity doUpdateAuthorisation(AuthorisationEntity authorisationEntity, UpdateAuthorisationRequest updateAuthorisationRequest);

    // ToDo properly handle generic type https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1175
    ConfirmationExpirationService<?> getConfirmationExpirationService();

    List<AuthorisationEntity> getAuthorisationsByParentId(String parentId);

    Optional<AuthorisationEntity> getAuthorisationById(String authorisationId);
}
