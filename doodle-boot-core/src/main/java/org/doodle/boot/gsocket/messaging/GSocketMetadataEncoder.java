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
import io.netty.buffer.ByteBufAllocator;
import org.doodle.design.messaging.PacketMetadataEncoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

final class GSocketMetadataEncoder implements PacketMetadataEncoder {

  private final GSocketStrategies strategies;

  private final ByteBufAllocator allocator;

  private short group;
  private short cmd;

  GSocketMetadataEncoder(GSocketStrategies strategies) {
    this.strategies = strategies;
    this.allocator =
        strategies.dataBufferFactory() instanceof NettyDataBufferFactory nettyDBF
            ? nettyDBF.getByteBufAllocator()
            : ByteBufAllocator.DEFAULT;
  }

  @Override
  public GSocketStrategies strategies() {
    return this.strategies;
  }

  @Override
  public GSocketMetadataEncoder route(short group, short cmd) {
    this.group = group;
    this.cmd = cmd;
    return this;
  }

  @Override
  public GSocketMetadataEncoder metadata(Object metadata, MimeType metadataMimeType) {
    return this;
  }

  @Override
  public Mono<DataBuffer> encode() {
    return Mono.fromCallable(this::encodeEntries);
  }

  private DataBuffer encodeEntries() {
    return asDataBuffer(encodeRoute());
  }

  private ByteBuf encodeRoute() {
    ByteBuf buffer = this.allocator.buffer();
    buffer.writeShort(group).writeShort(cmd);
    return buffer;
  }

  private DataBuffer asDataBuffer(ByteBuf byteBuf) {
    if (this.strategies.dataBufferFactory() instanceof NettyDataBufferFactory nettyDBF) {
      return nettyDBF.wrap(byteBuf);
    } else {
      DataBuffer dataBuffer = this.strategies.dataBufferFactory().wrap(byteBuf.nioBuffer());
      byteBuf.release();
      return dataBuffer;
    }
  }
}
