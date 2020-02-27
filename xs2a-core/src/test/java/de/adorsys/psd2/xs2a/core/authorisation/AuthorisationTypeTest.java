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

package de.adorsys.psd2.xs2a.core.authorisation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorisationTypeTest {

    @Test
    void fromValue() {
        assertEquals(AuthorisationType.AIS, AuthorisationType.fromValue("ais"));
        assertEquals(AuthorisationType.AIS, AuthorisationType.fromValue("AIS"));

        assertEquals(AuthorisationType.PIS_CANCELLATION, AuthorisationType.fromValue("pis_cancellation"));
        assertEquals(AuthorisationType.PIS_CANCELLATION, AuthorisationType.fromValue("PIS_CANCELLATION"));

        assertEquals(AuthorisationType.PIS_CREATION, AuthorisationType.fromValue("pis_creation"));
        assertEquals(AuthorisationType.PIS_CREATION, AuthorisationType.fromValue("PIS_CREATION"));
    }
}