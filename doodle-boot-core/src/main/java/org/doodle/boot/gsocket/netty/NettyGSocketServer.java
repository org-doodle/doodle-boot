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

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Objects;
import org.doodle.boot.gsocket.GSocketServer;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

public class NettyGSocketServer implements GSocketServer {

  private final Mono<? extends DisposableServer> starter;
  private Duration lifecycle;
  private DisposableServer server;

  NettyGSocketServer(Mono<? extends DisposableServer> starter, Duration lifecycle) {
    this.starter = Objects.requireNonNull(starter);
    this.lifecycle = lifecycle;
  }

  @Override
  public void start() {
    this.server = block(this.starter, this.lifecycle);
    startDaemonAwaitThread(server);
  }

  private void startDaemonAwaitThread(DisposableServer server) {
    Thread awaitThread = new Thread(() -> server.onDispose().block(), "GSocket");
    awaitThread.setContextClassLoader(this.getClass().getClassLoader());
    awaitThread.setDaemon(false);
    awaitThread.start();
  }

  @Override
  public void stop() {
    if (Objects.nonNull(this.server)) {
      this.server.dispose();
      this.server = null;
    }
  }

  @Override
  public InetSocketAddress address() {
    return null;
  }

  private <T> T block(Mono<T> mono, Duration timeout) {
    return timeout != null ? mono.block(timeout) : mono.block();
  }
}
