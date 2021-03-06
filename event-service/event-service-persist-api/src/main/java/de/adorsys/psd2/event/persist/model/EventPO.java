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

package de.adorsys.psd2.event.persist.model;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class EventPO {
    private Long id;
    private OffsetDateTime timestamp;
    @Nullable
    private String consentId;
    @Nullable
    private String paymentId;
    @Nullable
    private byte[] payload;
    private EventOrigin eventOrigin;
    private EventType eventType;
    private String instanceId;
    private String tppAuthorisationNumber;
    private String xRequestId;
    @Nullable
    private PsuIdDataPO psuIdData;
    private String internalRequestId;
}
