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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import lombok.Setter;
import org.doodle.boot.gsocket.ConfigurableGSocketServerFactory;
import org.doodle.boot.gsocket.GSocketServer;
import org.doodle.boot.gsocket.GSocketServerFactory;
import org.doodle.boot.gsocket.netty.internal.ServerTransport;
import org.doodle.boot.gsocket.netty.internal.TcpServerTransport;
import org.doodle.boot.gsocket.netty.internal.WebSocketServerTransport;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

@Setter
public class NettyGSocketServerFactory
    implements GSocketServerFactory, ConfigurableGSocketServerFactory {

  private Integer port = 8001;
  private InetAddress address;
  private GSocketServer.Transport transport = GSocketServer.Transport.WEBSOCKET;
  private Duration lifecycle;

  @Override
  public GSocketServer create(ServerTransport.ConnectionAcceptor acceptor) {
    ServerTransport serverTransport = createTransport();
    Mono<? extends DisposableServer> starter = serverTransport.start(acceptor);
    return new NettyGSocketServer(starter, this.lifecycle);
  }

  private ServerTransport createTransport() {
    return (this.transport == GSocketServer.Transport.WEBSOCKET)
        ? createWebSocketTransport()
        : createTcpTransport();
  }

  private ServerTransport createTcpTransport() {
    TcpServer tcpServer = TcpServer.create();
    return TcpServerTransport.create(tcpServer.bindAddress(this::getListenAddress));
  }

  private ServerTransport createWebSocketTransport() {
    HttpServer httpServer = HttpServer.create();
    return WebSocketServerTransport.create(httpServer.bindAddress(this::getListenAddress));
  }

  private InetSocketAddress getListenAddress() {
    return this.address != null
        ? new InetSocketAddress(this.address.getHostAddress(), this.port)
        : new InetSocketAddress(this.port);
  }
}
