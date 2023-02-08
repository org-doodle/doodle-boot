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
import java.net.SocketAddress;
import java.util.Objects;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;

public abstract class BaseDuplexConnection implements DuplexConnection {
  protected final Connection connection;
  protected Sinks.Empty<Void> onClose = Sinks.empty();
  protected Sinks.Many<ByteBuf> sender = Sinks.many().unicast().onBackpressureBuffer();

  public BaseDuplexConnection(Connection connection) {
    onClose().doFinally(s -> doOnClose()).subscribe();
    this.connection = Objects.requireNonNull(connection);

    this.connection
        .channel()
        .closeFuture()
        .addListener(
            future -> {
              if (!isDisposed()) {
                dispose();
              }
            });
  }

  private void doOnClose() {
    this.sender.tryEmitComplete();
    this.connection.dispose();
  }

  public Mono<Void> onClose() {
    return onClose.asMono();
  }

  @Override
  public void sendFrame(ByteBuf byteBuf) {
    this.sender.tryEmitNext(byteBuf);
  }

  @Override
  public ByteBufAllocator alloc() {
    return this.connection.channel().alloc();
  }

  @Override
  public SocketAddress remoteAddress() {
    return this.connection.channel().remoteAddress();
  }

  @Override
  public void dispose() {
    onClose.tryEmitEmpty();
  }
}
