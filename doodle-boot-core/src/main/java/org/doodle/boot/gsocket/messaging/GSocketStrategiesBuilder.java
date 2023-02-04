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

import io.netty.buffer.PooledByteBufAllocator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.doodle.design.messaging.PacketMetadataExtractor;
import org.doodle.design.messaging.PacketStrategies;
import org.springframework.core.codec.*;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.RouteMatcher;
import org.springframework.util.SimpleRouteMatcher;

public final class GSocketStrategiesBuilder implements PacketStrategies.Builder {
  private final List<Encoder<?>> encoders = new ArrayList<>();
  private final List<Decoder<?>> decoders = new ArrayList<>();
  private RouteMatcher routeMatcher;
  private DataBufferFactory dataBufferFactory;
  private PacketMetadataExtractor metadataExtractor;

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
  public GSocketStrategiesBuilder encoder(Encoder<?>... encoder) {
    this.encoders.addAll(Arrays.asList(encoder));
    return this;
  }

  @Override
  public GSocketStrategiesBuilder decoder(Decoder<?>... decoder) {
    this.decoders.addAll(Arrays.asList(decoder));
    return this;
  }

  @Override
  public GSocketStrategiesBuilder routeMatcher(RouteMatcher routeMatcher) {
    this.routeMatcher = routeMatcher;
    return this;
  }

  @Override
  public GSocketStrategiesBuilder dataBufferFactory(DataBufferFactory dataBufferFactory) {
    this.dataBufferFactory = dataBufferFactory;
    return this;
  }

  @Override
  public GSocketStrategiesBuilder metadataExtractor(PacketMetadataExtractor metadataExtractor) {
    this.metadataExtractor = metadataExtractor;
    return this;
  }

  @Override
  public GSocketStrategies build() {
    RouteMatcher routeMatcher =
        Objects.nonNull(this.routeMatcher) ? this.routeMatcher : initRouteMatcher();

    DataBufferFactory dataBufferFactory =
        Objects.nonNull(this.dataBufferFactory)
            ? this.dataBufferFactory
            : new NettyDataBufferFactory(PooledByteBufAllocator.DEFAULT);

    PacketMetadataExtractor metadataExtractor =
        Objects.nonNull(this.metadataExtractor)
            ? this.metadataExtractor
            : new GSocketMetadataExtractor();

    return new GSocketStrategies(
        encoders, decoders, routeMatcher, dataBufferFactory, metadataExtractor);
  }

  private RouteMatcher initRouteMatcher() {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    pathMatcher.setPathSeparator(".");
    return new SimpleRouteMatcher(pathMatcher);
  }
}
