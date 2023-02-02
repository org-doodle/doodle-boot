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

import org.doodle.boot.gsocket.GSocketServerFactory;
import org.doodle.boot.gsocket.context.GSocketServerBootstrap;
import org.doodle.boot.gsocket.messaging.GSocketMessageHandler;
import org.doodle.boot.gsocket.messaging.GSocketStrategies;
import org.doodle.boot.gsocket.netty.NettyGSocketServerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

@AutoConfiguration(after = GSocketMessagingAutoConfiguration.class)
@ConditionalOnClass({HttpServer.class, TcpServer.class})
@EnableConfigurationProperties(GSocketProperties.class)
public class GSocketServerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public GSocketServerFactory gSocketServerFactory(GSocketProperties properties) {
    NettyGSocketServerFactory serverFactory = new NettyGSocketServerFactory();
    PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    mapper.from(properties.getServer()::getPort).to(serverFactory::setPort);
    mapper.from(properties.getServer()::getAddress).to(serverFactory::setAddress);
    mapper.from(properties.getServer()::getTransport).to(serverFactory::setTransport);
    return serverFactory;
  }

  @Bean
  @ConditionalOnMissingBean
  public GSocketServerBootstrap gSocketServerBootstrap(
      GSocketServerFactory serverFactory,
      GSocketMessageHandler messageHandler,
      GSocketStrategies strategies) {
    return new GSocketServerBootstrap(serverFactory, messageHandler.responder());
  }
}
