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
import org.doodle.boot.gsocket.netty.NettyGSocketPayload;
import org.doodle.boot.gsocket.netty.internal.GSocketFrameCodec;
import org.springframework.cglib.core.internal.Function;

@FunctionalInterface
public interface GSocketPayloadDecoder extends Function<ByteBuf, GSocketPayload> {

  class DefaultDecoder implements GSocketPayloadDecoder {
    @Override
    public GSocketPayload apply(ByteBuf byteBuf) {
      ByteBuf metadata = GSocketFrameCodec.metadata(byteBuf);
      ByteBuf data = GSocketFrameCodec.data(byteBuf);
      return DefaultGSocketPayload.create(data, metadata);
    }
  }

  class NettyDecoder implements GSocketPayloadDecoder {
    @Override
    public GSocketPayload apply(ByteBuf byteBuf) {
      ByteBuf metadata = GSocketFrameCodec.metadata(byteBuf);
      metadata.retain();
      ByteBuf data = GSocketFrameCodec.data(byteBuf);
      data.retain();
      return NettyGSocketPayload.create(data, metadata);
    }
  }
}
