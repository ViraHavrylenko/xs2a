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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;

public class PsuIdDataBuilder {
    private static final String PSU_ID = "PSU-123";
    private static final String PSU_ID_TYPE = "Some type";
    private static final String PSU_CORPORATE_ID = "Some corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "Some corporate id type";
    private static final String PSU_IP_ADDRESS = "1.1.1.1";

    public static PsuIdData buildPsuIdData() {
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        psuIdData.setAdditionalPsuIdData(new AdditionalPsuIdData(PSU_IP_ADDRESS, null, null, null, null, null, null, null, null, null));
        return psuIdData;
    }

    public static PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    public static PsuIdData buildEmptyPsuIdData() {
        PsuIdData psuIdData = new PsuIdData(null, null, null, null);
        psuIdData.setAdditionalPsuIdData(new AdditionalPsuIdData(PSU_IP_ADDRESS, null, null, null, null, null, null, null, null, null));
        return psuIdData;
    }
}
