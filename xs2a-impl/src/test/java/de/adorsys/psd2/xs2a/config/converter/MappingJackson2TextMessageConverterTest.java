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

package de.adorsys.psd2.xs2a.config.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingJackson2TextMessageConverterTest {
    @Test
    void checkSupportedMediaTypes() {
        ObjectMapper mockedObjectMapper = Mockito.mock(ObjectMapper.class);
        MappingJackson2TextMessageConverter messageConverter = new MappingJackson2TextMessageConverter(mockedObjectMapper);
        List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();

        assertEquals(1, supportedMediaTypes.size());
        assertTrue(supportedMediaTypes.contains(MediaType.TEXT_PLAIN));
    }
}
