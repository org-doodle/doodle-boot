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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public class WebSocketRouteTransport extends BaseWebsocketServerTransport<WebSocketRouteTransport> {
  private final HttpServer httpServer;
  private final Consumer<? super HttpServerRoutes> routesBuilder;
  private final String path;

  public WebSocketRouteTransport(
      HttpServer httpServer, Consumer<? super HttpServerRoutes> routesBuilder, String path) {
    this.httpServer = Objects.requireNonNull(httpServer);
    this.routesBuilder = Objects.requireNonNull(routesBuilder);
    this.path = Objects.requireNonNull(path);
  }

  @Override
  public Mono<? extends DisposableServer> start(ConnectionAcceptor acceptor) {
    Objects.requireNonNull(acceptor);
    return this.httpServer
        .route(
            routes -> {
              routesBuilder.accept(routes);
              routes.ws(path, newHandler(acceptor), specBuilder.build());
            })
        .bind();
  }

  public static BiFunction<WebsocketInbound, WebsocketOutbound, Publisher<Void>> newHandler(
      ConnectionAcceptor acceptor) {
    return (inbound, outbound) ->
        acceptor
            .apply(new WebSocketDuplexConnection((Connection) inbound))
            .then(outbound.neverComplete());
  }
}
