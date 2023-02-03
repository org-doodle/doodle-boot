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
import lombok.experimental.UtilityClass;
import org.doodle.boot.gsocket.netty.internal.GSocketFrameCodec;

@UtilityClass
public class GSocketPayloadCodec {

  public static ByteBuf encode(ByteBufAllocator allocator, ByteBuf metadata, ByteBuf data) {
    ByteBuf body = encodeBody(allocator, metadata, data);
    return GSocketFrameCodec.encode(allocator, body.readableBytes(), body);
  }

  public static ByteBuf encodeBody(ByteBufAllocator allocator, ByteBuf metadata, ByteBuf data) {
    ByteBuf buf = GSocketFrameCodec.encode(allocator, metadata.readableBytes(), metadata);
    return allocator.compositeBuffer(2).addComponents(true, buf, data);
  }
}
