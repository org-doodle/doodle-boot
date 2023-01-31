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
package org.doodle.boot.autoconfigure.gsocket;

import org.doodle.boot.gsocket.messaging.GSocketMessageHandler;
import org.doodle.boot.gsocket.messaging.GSocketMessagingHandlerCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class GSocketMessagingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public GSocketMessageHandler gSocketMessageHandler(
      ObjectProvider<GSocketMessagingHandlerCustomizer> customizers) {
    GSocketMessageHandler messageHandler = new GSocketMessageHandler();
    customizers.orderedStream().forEach(customizer -> customizer.customize(messageHandler));
    return messageHandler;
  }
}
