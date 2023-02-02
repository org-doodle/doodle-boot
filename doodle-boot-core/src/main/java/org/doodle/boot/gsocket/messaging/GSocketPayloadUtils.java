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
import java.nio.ByteBuffer;
import lombok.experimental.UtilityClass;
import org.doodle.boot.gsocket.netty.NettyGSocketPayload;
import org.springframework.core.io.buffer.*;

@UtilityClass
public class GSocketPayloadUtils {

  public static DataBuffer retainDataAndReleasePayload(
      GSocketPayload payload, DataBufferFactory bufferFactory) {
    try {
      if (bufferFactory instanceof NettyDataBufferFactory) {
        ByteBuf byteBuf = payload.sliceData().retain();
        return ((NettyDataBufferFactory) bufferFactory).wrap(byteBuf);
      } else {
        return bufferFactory.wrap(payload.getData());
      }
    } finally {
      if (payload.refCnt() > 0) {
        payload.release();
      }
    }
  }

  public static GSocketPayload createPayload(DataBuffer data, DataBuffer metadata) {
    return data instanceof NettyDataBuffer || metadata instanceof NettyDataBuffer
        ? NettyGSocketPayload.create(asByteBuf(data), asByteBuf(metadata))
        : DefaultGSocketPayload.create(asByteBuffer(data), asByteBuffer(metadata));
  }

  static ByteBuf asByteBuf(DataBuffer buffer) {
    return NettyDataBufferFactory.toByteBuf(buffer);
  }

  static ByteBuffer asByteBuffer(DataBuffer buffer) {
    return buffer instanceof DefaultDataBuffer
        ? ((DefaultDataBuffer) buffer).getNativeBuffer()
        : buffer.toByteBuffer();
  }
}
