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
package org.doodle.boot.gsocket.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import org.doodle.boot.gsocket.messaging.GSocketPayload;
import org.springframework.lang.Nullable;

public class NettyGSocketPayload extends AbstractReferenceCounted implements GSocketPayload {

  private static final Recycler<NettyGSocketPayload> RECYCLER =
      new Recycler<>() {
        protected NettyGSocketPayload newObject(Handle<NettyGSocketPayload> handle) {
          return new NettyGSocketPayload(handle);
        }
      };

  private final Handle<NettyGSocketPayload> handle;
  private ByteBuf data;
  private ByteBuf metadata;

  private NettyGSocketPayload(final Handle<NettyGSocketPayload> handle) {
    this.handle = handle;
  }

  public static NettyGSocketPayload create(String data) {
    return create(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, data), null);
  }

  public static NettyGSocketPayload create(String data, @Nullable String metadata) {
    return create(
        ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, data),
        Objects.nonNull(metadata)
            ? ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, metadata)
            : null);
  }

  public static NettyGSocketPayload create(CharSequence data, Charset dataCharset) {
    return create(
        ByteBufUtil.encodeString(ByteBufAllocator.DEFAULT, CharBuffer.wrap(data), dataCharset),
        null);
  }

  public static NettyGSocketPayload create(
      CharSequence data,
      Charset dataCharset,
      @Nullable CharSequence metadata,
      Charset metadataCharset) {
    return create(
        ByteBufUtil.encodeString(ByteBufAllocator.DEFAULT, CharBuffer.wrap(data), dataCharset),
        Objects.nonNull(metadata)
            ? ByteBufUtil.encodeString(
                ByteBufAllocator.DEFAULT, CharBuffer.wrap(metadata), metadataCharset)
            : null);
  }

  public static NettyGSocketPayload create(byte[] data) {
    return create(Unpooled.wrappedBuffer(data), null);
  }

  public static NettyGSocketPayload create(byte[] data, @Nullable byte[] metadata) {
    return create(
        Unpooled.wrappedBuffer(data),
        Objects.nonNull(metadata) ? Unpooled.wrappedBuffer(metadata) : null);
  }

  public static NettyGSocketPayload create(ByteBuffer data) {
    return create(Unpooled.wrappedBuffer(data), null);
  }

  public static NettyGSocketPayload create(ByteBuffer data, @Nullable ByteBuffer metadata) {
    return create(
        Unpooled.wrappedBuffer(data),
        Objects.nonNull(metadata) ? Unpooled.wrappedBuffer(metadata) : null);
  }

  public static NettyGSocketPayload create(ByteBuf data) {
    return create(data, null);
  }

  public static NettyGSocketPayload create(ByteBuf data, @Nullable ByteBuf metadata) {
    NettyGSocketPayload payload = RECYCLER.get();
    payload.data = data;
    payload.metadata = metadata;
    // ensure data and metadata is set before refCnt change
    payload.setRefCnt(1);
    return payload;
  }

  public static NettyGSocketPayload create(NettyGSocketPayload payload) {
    return create(
        payload.sliceData().retain(),
        payload.hasMetadata() ? payload.sliceMetadata().retain() : null);
  }

  @Override
  public boolean hasMetadata() {
    ensureAccessible();
    return Objects.nonNull(metadata);
  }

  @Override
  public ByteBuf sliceMetadata() {
    ensureAccessible();
    return Objects.nonNull(metadata) ? metadata.slice() : Unpooled.EMPTY_BUFFER;
  }

  @Override
  public ByteBuf data() {
    ensureAccessible();
    return data;
  }

  @Override
  public ByteBuf metadata() {
    ensureAccessible();
    return Objects.nonNull(metadata) ? metadata : Unpooled.EMPTY_BUFFER;
  }

  @Override
  public ByteBuf sliceData() {
    ensureAccessible();
    return data.slice();
  }

  @Override
  public NettyGSocketPayload retain() {
    super.retain();
    return this;
  }

  @Override
  public NettyGSocketPayload retain(int increment) {
    super.retain(increment);
    return this;
  }

  @Override
  public NettyGSocketPayload touch() {
    ensureAccessible();
    data.touch();
    if (Objects.nonNull(metadata)) {
      metadata.touch();
    }
    return this;
  }

  @Override
  public NettyGSocketPayload touch(Object hint) {
    ensureAccessible();
    data.touch(hint);
    if (Objects.nonNull(metadata)) {
      metadata.touch(hint);
    }
    return this;
  }

  @Override
  protected void deallocate() {
    data.release();
    data = null;
    if (Objects.nonNull(metadata)) {
      metadata.release();
      metadata = null;
    }
    handle.recycle(this);
  }

  void ensureAccessible() {
    if (!isAccessible()) {
      throw new IllegalReferenceCountException(0);
    }
  }

  boolean isAccessible() {
    return refCnt() != 0;
  }
}
