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
  private final int maxFrameLength;

  public static TcpServerTransport create(TcpServer tcpServer) {
    return new TcpServerTransport(tcpServer, GSocketFrameCodec.FRAME_LENGTH_MASK);
  }

  private TcpServerTransport(TcpServer tcpServer, int maxFrameLength) {
    this.tcpServer = Objects.requireNonNull(tcpServer);
    this.maxFrameLength = maxFrameLength;
  }

  @Override
  public int maxFrameLength() {
    return this.maxFrameLength;
  }

  @Override
  public Mono<? extends DisposableServer> start(ConnectionAcceptor acceptor) {
    return tcpServer
        .doOnConnection(
            (c) -> {
              c.addHandlerLast(new GSocketLengthCodec(maxFrameLength));
              acceptor
                  .apply(new TcpDuplexConnection(c))
                  .then(Mono.<Void>never())
                  .subscribe(c.disposeSubscriber());
            })
        .bind();
  }
}
