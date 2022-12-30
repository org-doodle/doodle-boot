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
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

public class TcpClientTransport implements ClientTransport {

  private final TcpClient tcpClient;
  private final int maxFrameLength;

  public static TcpClientTransport create(TcpClient tcpClient) {
    return new TcpClientTransport(tcpClient, GSocketFrameCodec.FRAME_LENGTH_MASK);
  }

  private TcpClientTransport(TcpClient tcpClient, int maxFrameLength) {
    this.tcpClient = Objects.requireNonNull(tcpClient);
    this.maxFrameLength = maxFrameLength;
  }

  @Override
  public int maxFrameLength() {
    return this.maxFrameLength;
  }

  @Override
  public Mono<? extends Connection> connect() {
    return tcpClient
        .doOnConnected(c -> c.addHandlerLast(new GSocketLengthCodec(maxFrameLength)))
        .connect();
  }
}
