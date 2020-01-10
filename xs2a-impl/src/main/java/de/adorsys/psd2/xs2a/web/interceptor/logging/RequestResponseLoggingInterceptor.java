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

package de.adorsys.psd2.xs2a.web.interceptor.logging;

import de.adorsys.psd2.xs2a.component.logger.request.RequestResponseLogMessage;
import de.adorsys.psd2.xs2a.component.logger.request.RequestResponseLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor for logging request and response information into the request-log
 */
@RequiredArgsConstructor
public class RequestResponseLoggingInterceptor extends HandlerInterceptorAdapter {
    private final RequestResponseLogger requestResponseLogger;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestResponseLogMessage message = RequestResponseLogMessage.builder(request, response)
                                                .withRequestUri()
                                                .withRequestHeaders()
                                                .withRequestPayload()
                                                .withResponseStatus()
                                                .withResponseHeaders()
                                                .withResponseBody()
                                                .build();
        requestResponseLogger.logMessage(message);
    }
}
