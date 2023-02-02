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
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.springframework.lang.Nullable;

public class DefaultGSocketPayload implements GSocketPayload {
  private final ByteBuffer data;
  private final ByteBuffer metadata;

  private DefaultGSocketPayload(ByteBuffer data, @Nullable ByteBuffer metadata) {
    this.data = data;
    this.metadata = metadata;
  }

  public static DefaultGSocketPayload create(CharSequence data) {
    return create(StandardCharsets.UTF_8.encode(CharBuffer.wrap(data)), null);
  }

  public static DefaultGSocketPayload create(CharSequence data, @Nullable CharSequence metadata) {
    return create(
        StandardCharsets.UTF_8.encode(CharBuffer.wrap(data)),
        Objects.nonNull(metadata)
            ? StandardCharsets.UTF_8.encode(CharBuffer.wrap(metadata))
            : null);
  }

  public static DefaultGSocketPayload create(CharSequence data, Charset dataCharset) {
    return create(dataCharset.encode(CharBuffer.wrap(data)), null);
  }

  public static DefaultGSocketPayload create(
      CharSequence data,
      Charset dataCharset,
      @Nullable CharSequence metadata,
      Charset metadataCharset) {
    return create(
        dataCharset.encode(CharBuffer.wrap(data)),
        Objects.nonNull(metadata) ? metadataCharset.encode(CharBuffer.wrap(metadata)) : null);
  }

  public static DefaultGSocketPayload create(byte[] data) {
    return create(ByteBuffer.wrap(data), null);
  }

  public static DefaultGSocketPayload create(byte[] data, @Nullable byte[] metadata) {
    return create(
        ByteBuffer.wrap(data), Objects.nonNull(metadata) ? ByteBuffer.wrap(metadata) : null);
  }

  public static DefaultGSocketPayload create(ByteBuffer data) {
    return create(data, null);
  }

  public static DefaultGSocketPayload create(ByteBuffer data, @Nullable ByteBuffer metadata) {
    return new DefaultGSocketPayload(data, metadata);
  }

  public static DefaultGSocketPayload create(ByteBuf data) {
    return create(data, null);
  }

  public static DefaultGSocketPayload create(ByteBuf data, @Nullable ByteBuf metadata) {
    try {
      return create(toBytes(data), Objects.nonNull(metadata) ? toBytes(metadata) : null);
    } finally {
      data.release();
      if (Objects.nonNull(metadata)) {
        metadata.release();
      }
    }
  }

  private static byte[] toBytes(ByteBuf byteBuf) {
    byte[] bytes = new byte[byteBuf.readableBytes()];
    byteBuf.markReaderIndex();
    byteBuf.readBytes(bytes);
    byteBuf.resetReaderIndex();
    return bytes;
  }

  @Override
  public boolean hasMetadata() {
    return Objects.nonNull(metadata);
  }

  @Override
  public ByteBuf data() {
    return sliceData();
  }

  @Override
  public ByteBuf sliceData() {
    return Unpooled.wrappedBuffer(data);
  }

  @Override
  public ByteBuf metadata() {
    return sliceMetadata();
  }

  @Override
  public ByteBuf sliceMetadata() {
    return Objects.nonNull(metadata) ? Unpooled.wrappedBuffer(metadata) : Unpooled.EMPTY_BUFFER;
  }

  @Override
  public int refCnt() {
    return 1;
  }

  @Override
  public DefaultGSocketPayload retain() {
    return this;
  }

  @Override
  public DefaultGSocketPayload retain(int i) {
    return this;
  }

  @Override
  public DefaultGSocketPayload touch() {
    return this;
  }

  @Override
  public DefaultGSocketPayload touch(Object o) {
    return this;
  }

  @Override
  public boolean release() {
    return false;
  }

  @Override
  public boolean release(int i) {
    return false;
  }
}
