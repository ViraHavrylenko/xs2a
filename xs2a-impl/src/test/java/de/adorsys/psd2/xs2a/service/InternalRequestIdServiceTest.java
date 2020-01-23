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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.InternalRequestIdHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalRequestIdServiceTest {
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("b571c834-4eb1-468f-91b0-f5e83589bc22");

    @Mock
    private InternalRequestIdHolder internalRequestIdHolder;
    @InjectMocks
    private InternalRequestIdService internalRequestIdService;

    @Test
    void getInternalRequestId_shouldReturnIdFromHolder() {
        // Given
        when(internalRequestIdHolder.getInternalRequestId()).thenReturn(INTERNAL_REQUEST_ID);

        // When
        UUID actualInternalRequestId = internalRequestIdService.getInternalRequestId();

        // Then
        //noinspection ResultOfMethodCallIgnored
        verify(internalRequestIdHolder).getInternalRequestId();
        assertEquals(INTERNAL_REQUEST_ID, actualInternalRequestId);
    }

    @Test
    void getInternalRequestId_withNullIdInHolder_shouldGenerateId() {
        // When
        UUID actualInternalRequestId = internalRequestIdService.getInternalRequestId();

        // Then
        assertNotNull(actualInternalRequestId);
        verify(internalRequestIdHolder).setInternalRequestId(any(UUID.class));
    }
}
