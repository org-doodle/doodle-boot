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
import java.util.Map;
import org.doodle.design.messaging.PacketMetadataExtractor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.DestinationPatternsMessageCondition;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.MimeType;
import org.springframework.util.RouteMatcher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

class MessagingGSocket {

  private final Connection connection;
  private final MimeType dataMimeType;
  private final MimeType metadataMimeType;
  private final GSocketMessageHandler messageHandler;
  private final GSocketStrategies strategies;
  private final GSocketRequester requester;

  MessagingGSocket(
      Connection connection,
      GSocketMessageHandler messageHandler,
      MimeType dataMimeType,
      MimeType metadataMimeType,
      GSocketStrategies strategies,
      GSocketPayloadDecoder payloadDecoder,
      GSocketRequester requester) {
    this.connection = connection;
    this.dataMimeType = dataMimeType;
    this.metadataMimeType = metadataMimeType;
    this.messageHandler = messageHandler;
    this.strategies = strategies;
    this.requester = requester;

    this.connection
        .inbound()
        .receiveObject()
        .cast(ByteBuf.class)
        .map(payloadDecoder::apply)
        .subscribe(this::onReceivePayload);
  }

  private void onReceivePayload(GSocketPayload payload) {
    MessageHeaders header = createHeaders(payload);
    DataBuffer dataBuffer =
        GSocketPayloadUtils.retainDataAndReleasePayload(payload, strategies.dataBufferFactory());
    int refCount = refCount(dataBuffer);
    Message<?> message = MessageBuilder.createMessage(dataBuffer, header);
    Mono.defer(() -> this.messageHandler.handleMessage(message))
        .doFinally(
            s -> {
              if (refCount(dataBuffer) == refCount) {
                DataBufferUtils.release(dataBuffer);
              }
            })
        .subscribe();
  }

  private MessageHeaders createHeaders(GSocketPayload payload) {
    MessageHeaderAccessor header = new MessageHeaderAccessor();
    header.setLeaveMutable(true);

    Map<String, Object> metadataValues =
        this.strategies.metadataExtractor().extract(payload, metadataMimeType);
    metadataValues.putIfAbsent(PacketMetadataExtractor.ROUTE_KEY, "");
    header.setContentType(dataMimeType);
    for (Map.Entry<String, Object> entry : metadataValues.entrySet()) {
      RouteMatcher.Route route =
          this.strategies.routeMatcher().parseRoute((String) entry.getValue());
      if (entry.getKey().equals(PacketMetadataExtractor.ROUTE_KEY)) {
        header.setHeader(DestinationPatternsMessageCondition.LOOKUP_DESTINATION_HEADER, route);
      } else {
        header.setHeader(entry.getKey(), entry.getValue());
      }
    }
    return header.getMessageHeaders();
  }

  private int refCount(DataBuffer dataBuffer) {
    return dataBuffer instanceof NettyDataBuffer
        ? ((NettyDataBuffer) dataBuffer).getNativeBuffer().refCnt()
        : 1;
  }
}
