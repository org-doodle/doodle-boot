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
import java.util.Objects;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

public class WebSocketClientTransport implements ClientTransport {

  private final HttpClient httpClient;

  private HttpHeaders httpHeaders = new DefaultHttpHeaders();

  private final WebsocketClientSpec.Builder specBuilder =
      WebsocketClientSpec.builder().maxFramePayloadLength(GSocketFrameCodec.FRAME_LENGTH_MASK);

  public static WebSocketClientTransport create(HttpClient httpClient) {
    return new WebSocketClientTransport(httpClient);
  }

  private WebSocketClientTransport(HttpClient httpClient) {
    this.httpClient = Objects.requireNonNull(httpClient);
  }

  @Override
  public Mono<? extends Connection> connect() {
    return this.httpClient
        .headers(headers -> headers.add(this.httpHeaders))
        .websocket(specBuilder.build())
        .connect();
  }
}
