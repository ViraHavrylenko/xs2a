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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.jetbrains.annotations.NotNull;

public interface TppService {

    /**
     * Updates TPP by TPP authorisation number
     *
     * @param tppInfo TPP Info
     * @return <code>true</code> if TPP was found and was updated. <code>false</code> otherwise.
     */
    boolean updateTppInfo(@NotNull TppInfo tppInfo);
}
