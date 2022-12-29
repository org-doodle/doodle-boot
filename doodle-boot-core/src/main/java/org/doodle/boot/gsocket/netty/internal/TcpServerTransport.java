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

import java.util.Objects;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public class TcpServerTransport implements ServerTransport {
  private final TcpServer tcpServer;

  public static TcpServerTransport create(TcpServer tcpServer) {
    return new TcpServerTransport(tcpServer);
  }

  private TcpServerTransport(TcpServer tcpServer) {
    this.tcpServer = Objects.requireNonNull(tcpServer);
  }

  @Override
  public Mono<? extends DisposableServer> start(ConnectionAcceptor acceptor) {
    return tcpServer
        .doOnConnection(
            (c) -> {
              c.addHandlerLast(new GSocketLengthCodec());
              acceptor.apply(c).then(Mono.<Void>never()).subscribe(c.disposeSubscriber());
            })
        .bind();
  }
}
