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

package de.adorsys.psd2.xs2a.service.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardPaymentProductsResolverTest {
    private StandardPaymentProductsResolver standardPaymentProductsResolver;

    @BeforeEach
    void setUp() {
        standardPaymentProductsResolver = new StandardPaymentProductsResolver();
    }

    @Test
    void isRawPaymentProduct() {
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("instant-sepa-credit-transfers"));
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("target-2-payments"));
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("cross-border-credit-transfers"));
        assertFalse(standardPaymentProductsResolver.isRawPaymentProduct("sepa-credit-transfers"));
        assertTrue(standardPaymentProductsResolver.isRawPaymentProduct("pain"));
        assertTrue(standardPaymentProductsResolver.isRawPaymentProduct("dtazv"));
    }
}
