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
import java.util.List;
import org.doodle.design.messaging.PacketMetadataExtractor;
import org.doodle.design.messaging.PacketStrategies;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.util.RouteMatcher;

public class GSocketStrategies implements PacketStrategies {

  private final List<Encoder<?>> encoders;

  private final List<Decoder<?>> decoders;

  private final RouteMatcher routeMatcher;

  private final DataBufferFactory dataBufferFactory;

  private final PacketMetadataExtractor metadataExtractor;

  GSocketStrategies(
      List<Encoder<?>> encoders,
      List<Decoder<?>> decoders,
      RouteMatcher routeMatcher,
      DataBufferFactory dataBufferFactory,
      PacketMetadataExtractor metadataExtractor) {
    this.encoders = Collections.unmodifiableList(encoders);
    this.decoders = Collections.unmodifiableList(decoders);
    this.routeMatcher = routeMatcher;
    this.dataBufferFactory = dataBufferFactory;
    this.metadataExtractor = metadataExtractor;
  }

  @Override
  public List<Encoder<?>> encoders() {
    return this.encoders;
  }

  @Override
  public List<Decoder<?>> decoders() {
    return this.decoders;
  }

  @Override
  public RouteMatcher routeMatcher() {
    return this.routeMatcher;
  }

  @Override
  public DataBufferFactory dataBufferFactory() {
    return this.dataBufferFactory;
  }

  @Override
  public PacketMetadataExtractor metadataExtractor() {
    return this.metadataExtractor;
  }
}
