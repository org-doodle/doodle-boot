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

import java.util.ArrayList;
import java.util.List;
import org.doodle.design.messaging.PacketStrategies;
import org.springframework.core.codec.*;

public final class GSocketStrategiesBuilder implements PacketStrategies.Builder {
  private final List<Encoder<?>> encoders = new ArrayList<>();

  private final List<Decoder<?>> decoders = new ArrayList<>();

  public GSocketStrategiesBuilder() {
    this.encoders.add(CharSequenceEncoder.allMimeTypes());
    this.encoders.add(new ByteBufferEncoder());
    this.encoders.add(new ByteArrayEncoder());
    this.encoders.add(new DataBufferEncoder());

    this.decoders.add(StringDecoder.allMimeTypes());
    this.decoders.add(new ByteBufferDecoder());
    this.decoders.add(new ByteArrayDecoder());
    this.decoders.add(new DataBufferDecoder());
  }

  @Override
  public GSocketStrategies build() {
    return new GSocketStrategies(encoders, decoders, null, null);
  }
}
