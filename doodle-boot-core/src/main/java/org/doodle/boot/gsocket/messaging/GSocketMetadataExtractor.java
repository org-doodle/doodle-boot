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
import java.util.HashMap;
import java.util.Map;
import org.doodle.design.messaging.PacketMetadataExtractor;
import org.doodle.design.messaging.PacketPayload;
import org.springframework.util.MimeType;

public class GSocketMetadataExtractor implements PacketMetadataExtractor {

  @Override
  public Map<String, Object> extract(PacketPayload payload, MimeType metadataMimeType) {
    Map<String, Object> results = new HashMap<>();
    String route = readRoute(payload);
    results.put(PacketMetadataExtractor.ROUTE_KEY, route);
    return results;
  }

  private String readRoute(PacketPayload payload) {
    ByteBuf metadata = payload.metadata();
    metadata.markReaderIndex();
    metadata.skipBytes(3);
    short group = metadata.readShort();
    short cmd = metadata.readShort();
    metadata.resetReaderIndex();
    return group + "." + cmd;
  }
}
