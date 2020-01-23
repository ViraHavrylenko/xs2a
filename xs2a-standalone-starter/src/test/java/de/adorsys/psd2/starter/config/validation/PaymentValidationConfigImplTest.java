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

package de.adorsys.psd2.starter.config.validation;

import de.adorsys.psd2.xs2a.web.validator.body.payment.config.PaymentValidationConfig;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.adorsys.psd2.starter.config.validation"})
@PropertySource("application.properties")
class PaymentValidationConfigImplTest {

    @Autowired
    private PaymentValidationConfig paymentValidationConfig;

    private JsonReader jsonReader = new JsonReader();

    @Test
    void defaultPaymentValidationConfig() {
        PaymentValidationConfigImpl expectedPaymentValidationConfig = jsonReader.getObjectFromFile("json/validation/payment-validation-config.json",
                                                                                                   PaymentValidationConfigImpl.class);
        assertEquals(expectedPaymentValidationConfig, paymentValidationConfig);
    }
}
