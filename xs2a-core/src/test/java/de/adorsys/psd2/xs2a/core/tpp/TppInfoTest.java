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

package de.adorsys.psd2.xs2a.core.tpp;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TppInfoTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String AUTHORISATION_NUMBER_2 = "authorisation number 2";

    @Test
    void equals_withOnlyAuthorisationNumberAndAuthorityIdSame_shouldReturnTrue() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER);
        tppInfoFirst.setAuthorityId("authorisation number");
        tppInfoFirst.setTppName("some tpp name");
        tppInfoFirst.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfoFirst.setAuthorityName("some authority name");
        tppInfoFirst.setCountry("some country");
        tppInfoFirst.setOrganisation("some organisation");
        tppInfoFirst.setOrganisationUnit("some country unit");
        tppInfoFirst.setCity("some city");
        tppInfoFirst.setState("some state");
        tppInfoFirst.setIssuerCN("some issuer CN");

        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER);
        tppInfoSecond.setAuthorityId("authorisation number");
        tppInfoSecond.setTppName("some other tpp name");
        tppInfoSecond.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfoSecond.setAuthorityName("some other authority name");
        tppInfoSecond.setCountry("some other country");
        tppInfoSecond.setOrganisation("some other organisation");
        tppInfoSecond.setOrganisationUnit("some other country unit");
        tppInfoSecond.setCity("some other city");
        tppInfoSecond.setState("some other state");
        tppInfoSecond.setIssuerCN("some other issuer CN");

        assertEquals(tppInfoFirst, tppInfoSecond);
    }

    @Test
    void equals_withDifferentAuthorisationNumber_shouldReturnFalse() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER);
        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER_2);

        assertNotEquals(tppInfoFirst, tppInfoSecond);
    }

    private TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);

        return tppInfo;
    }
}
