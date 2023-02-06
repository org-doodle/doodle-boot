/*
 * Copyright (c) 2022-present Doodle. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.doodle.boot.gsocket.messaging;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.reactive.HandlerMethodArgumentResolver;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

public class GSocketRequesterMethodArgumentResolver implements HandlerMethodArgumentResolver {

  public static final String REQUESTER_HEADER = "requestHeader";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    Class<?> type = parameter.getParameterType();
    return GSocketRequester.class.equals(type);
  }

  @Override
  public Mono<Object> resolveArgument(MethodParameter parameter, Message<?> message) {
    Object headerValue = message.getHeaders().get(REQUESTER_HEADER);
    Assert.notNull(headerValue, "Missing '" + REQUESTER_HEADER + "'");
    Assert.isInstanceOf(
        GSocketRequester.class, headerValue, "Expect header value of type GSocketRequester");
    GSocketRequester requester = (GSocketRequester) headerValue;
    Class<?> type = parameter.getParameterType();
    if (GSocketRequester.class.equals(type)) {
      return Mono.just(requester);
    } else {
      return Mono.error(new IllegalArgumentException("Unexpected parameter type: " + parameter));
    }
  }
}
