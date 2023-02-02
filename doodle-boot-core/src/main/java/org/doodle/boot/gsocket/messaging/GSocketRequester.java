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

import java.util.Collections;
import org.doodle.design.messaging.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

public final class GSocketRequester implements PacketRequester {

  private final PacketSocket packetSocket;

  private final MimeType dataMimeType;

  private final MimeType metadataMimeType;

  private final GSocketStrategies strategies;

  GSocketRequester(
      PacketSocket packetSocket,
      MimeType dataMimeType,
      MimeType metadataMimeType,
      GSocketStrategies strategies) {
    this.packetSocket = packetSocket;
    this.dataMimeType = dataMimeType;
    this.metadataMimeType = metadataMimeType;
    this.strategies = strategies;
  }

  @Override
  public PacketSocket socket() {
    return this.packetSocket;
  }

  @Override
  public MimeType dataMimeType() {
    return this.dataMimeType;
  }

  @Override
  public MimeType metadataMimeType() {
    return this.metadataMimeType;
  }

  @Override
  public GSocketStrategies strategies() {
    return this.strategies;
  }

  @Override
  public RequestSpec route(short group, short cmd) {
    return new GSocketRequestSpec(group, cmd);
  }

  public static GSocketRequester wrap(
      PacketSocket packetSocket,
      MimeType dataMimeType,
      MimeType metadataMimeType,
      GSocketStrategies strategies) {
    return new GSocketRequester(packetSocket, dataMimeType, metadataMimeType, strategies);
  }

  private class GSocketRequestSpec implements PacketRequester.RequestSpec {

    private final PacketMetadataEncoder metadataEncoder = new GSocketMetadataEncoder(strategies);
    private Mono<GSocketPayload> payloadMono;

    private GSocketRequestSpec(short group, short cmd) {
      this.metadataEncoder.route(group, cmd);
    }

    @Override
    public RequestSpec data(Object data) {
      this.payloadMono = createPayload(Mono.fromCallable(() -> encodeData(data)));
      return this;
    }

    @SuppressWarnings("unchecked")
    private <T> DataBuffer encodeData(T data) {
      ResolvableType elementType = ResolvableType.forInstance(data);
      Encoder<?> encoder = strategies.encoder(elementType, dataMimeType());
      return ((Encoder<T>) encoder)
          .encodeValue(
              data,
              strategies.dataBufferFactory(),
              elementType,
              dataMimeType(),
              Collections.emptyMap());
    }

    private Mono<GSocketPayload> createPayload(Mono<DataBuffer> encodedData) {
      return Mono.zip(encodedData, this.metadataEncoder.encode())
          .map(tuple -> GSocketPayloadUtils.createPayload(tuple.getT1(), tuple.getT2()))
          .doOnDiscard(DataBuffer.class, DataBufferUtils::release)
          .doOnDiscard(PacketPayload.class, PacketPayload::release);
    }

    @Override
    public Mono<Void> send() {
      return packetSocket.send(payloadMono.block());
    }
  }
}
