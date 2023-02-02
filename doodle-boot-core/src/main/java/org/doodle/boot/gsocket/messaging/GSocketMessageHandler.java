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

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.doodle.boot.gsocket.netty.internal.ServerTransport;
import org.doodle.design.messaging.reactive.PacketMappingMessageHandler;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import reactor.core.publisher.Mono;

public class GSocketMessageHandler extends PacketMappingMessageHandler {

  private final List<Encoder<?>> encoders = new ArrayList<>();

  private GSocketStrategies strategies;

  private GSocketPayloadDecoder payloadDecoder;

  public void setStrategies(GSocketStrategies strategies) {
    this.strategies = strategies;
    this.encoders.clear();
    this.encoders.addAll(this.strategies.encoders());
    super.setDecoders(this.strategies.decoders());
  }

  public ServerTransport.ConnectionAcceptor responder() {
    return connection -> {
      connection
          .inbound()
          .receiveObject()
          .cast(ByteBuf.class)
          .map(payloadDecoder::apply)
          .subscribe(this::onReceivePayload);
      return Mono.never();
    };
  }

  private void onReceivePayload(GSocketPayload payload) {
    MessageHeaders header = createHeaders(payload);
    DataBuffer dataBuffer =
        GSocketPayloadUtils.retainDataAndReleasePayload(payload, strategies.dataBufferFactory());
    int refCount = refCount(dataBuffer);
    Message<?> message = MessageBuilder.createMessage(dataBuffer, header);
    Mono.defer(() -> handleMessage(message))
        .doFinally(
            s -> {
              if (refCount(dataBuffer) == refCount) {
                DataBufferUtils.release(dataBuffer);
              }
            })
        .block();
  }

  private MessageHeaders createHeaders(GSocketPayload payload) {
    MessageHeaderAccessor header = new MessageHeaderAccessor();
    return header.getMessageHeaders();
  }

  private int refCount(DataBuffer dataBuffer) {
    return dataBuffer instanceof NettyDataBuffer
        ? ((NettyDataBuffer) dataBuffer).getNativeBuffer().refCnt()
        : 1;
  }
}
