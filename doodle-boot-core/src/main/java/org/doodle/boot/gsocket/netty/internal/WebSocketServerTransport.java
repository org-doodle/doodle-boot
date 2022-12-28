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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class WebSocketServerTransport implements ServerTransport {
  private final HttpServer httpServer;

  private HttpHeaders httpHeaders = new DefaultHttpHeaders();

  public static WebSocketServerTransport create(HttpServer httpServer) {
    return new WebSocketServerTransport(httpServer);
  }

  private WebSocketServerTransport(HttpServer httpServer) {
    this.httpServer = httpServer;
  }

  @Override
  public Mono<? extends DisposableServer> start(ConnectionAcceptor acceptor) {
    return httpServer
        .handle(
            (request, response) -> {
              response.headers(httpHeaders);
              return response.sendWebsocket(
                  (inbound, outbound) ->
                      acceptor
                          .apply(new WebSocketDuplexConnection((Connection) inbound))
                          .then(outbound.neverComplete()));
            })
        .bind();
  }
}
