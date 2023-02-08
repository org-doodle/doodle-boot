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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.doodle.boot.gsocket.messaging.reactive.GSocketRequesterMethodArgumentResolver;
import org.doodle.boot.gsocket.netty.NettyGSocketPacketSocket;
import org.doodle.boot.gsocket.netty.internal.ServerTransport;
import org.doodle.design.messaging.reactive.PacketMappingMessageHandler;
import org.doodle.design.messaging.reactive.PacketPayloadReturnValueHandler;
import org.springframework.core.codec.Encoder;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;

public class GSocketMessageHandler extends PacketMappingMessageHandler {

  @Getter private final List<Encoder<?>> encoders = new ArrayList<>();

  private GSocketStrategies strategies = new GSocketStrategiesBuilder().build();

  @Setter private MimeType dataMimeType = MimeTypeUtils.APPLICATION_JSON;

  @Setter private MimeType metadataMimeType = MimeTypeUtils.APPLICATION_JSON;

  @Setter private GSocketPayloadDecoder payloadDecoder;

  public void setStrategies(GSocketStrategies strategies) {
    this.strategies = strategies;
    this.encoders.clear();
    this.encoders.addAll(this.strategies.encoders());
    super.setDecoders(this.strategies.decoders());
  }

  @Override
  public void afterPropertiesSet() {
    getArgumentResolverConfigurer().addCustomResolver(new GSocketRequesterMethodArgumentResolver());
    getReturnValueHandlerConfigurer()
        .addCustomHandler(
            new PacketPayloadReturnValueHandler(this.encoders, getReactiveAdapterRegistry()));
    super.afterPropertiesSet();
  }

  public ServerTransport.ConnectionAcceptor responder() {
    return connection -> {
      GSocketRequester requester =
          GSocketRequester.wrap(
              new NettyGSocketPacketSocket(connection),
              this.dataMimeType,
              this.metadataMimeType,
              this.strategies);
      MessagingGSocket responder =
          new MessagingGSocket(
              connection,
              this,
              this.dataMimeType,
              this.metadataMimeType,
              this.strategies,
              this.payloadDecoder,
              requester);
      return Mono.just(responder).then();
    };
  }
}
