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
package org.doodle.boot.gsocket.netty.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GSocketFrameCodec {

  public static final int FRAME_LENGTH_MASK = 0xFFFFFF;
  public static final int FRAME_LENGTH_SIZE = 3;

  public static void encodeLength(final ByteBuf byteBuf, final int length) {
    if ((length & ~FRAME_LENGTH_MASK) != 0) {
      throw new IllegalArgumentException("Length is larger than 24 bits");
    }
    byteBuf.writeByte(length >> 16);
    byteBuf.writeByte(length >> 8);
    byteBuf.writeByte(length);
  }

  private static int decodeLength(final ByteBuf byteBuf) {
    int length = (byteBuf.readByte() & 0xFF) << 16;
    length |= (byteBuf.readByte() & 0xFF) << 8;
    length |= byteBuf.readByte() & 0xFF;
    return length;
  }

  public static ByteBuf encode(ByteBufAllocator allocator, int length, ByteBuf frame) {
    ByteBuf buffer = allocator.buffer();
    encodeLength(buffer, length);
    return allocator.compositeBuffer(2).addComponents(true, buffer, frame);
  }

  public static int length(ByteBuf byteBuf) {
    byteBuf.markReaderIndex();
    int length = decodeLength(byteBuf);
    byteBuf.resetReaderIndex();
    return length;
  }

  public static ByteBuf frame(ByteBuf byteBuf) {
    byteBuf.markReaderIndex();
    byteBuf.skipBytes(3);
    ByteBuf slice = byteBuf.slice();
    byteBuf.resetReaderIndex();
    return slice;
  }

  public static ByteBuf metadata(ByteBuf byteBuf) {
    byteBuf.markReaderIndex();
    byteBuf.skipBytes(3);
    int metadataLength = decodeLength(byteBuf);
    ByteBuf slice = byteBuf.readSlice(metadataLength);
    byteBuf.resetReaderIndex();
    return slice;
  }

  public static ByteBuf data(ByteBuf byteBuf) {
    byteBuf.markReaderIndex();
    byteBuf.skipBytes(3);
    int metadataLength = decodeLength(byteBuf);
    byteBuf.skipBytes(metadataLength);
    ByteBuf slice = byteBuf.slice();
    byteBuf.resetReaderIndex();
    return slice;
  }
}
