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
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

public class WebSocketClientTransport implements ClientTransport {

  public static final String DEFAULT_PATH = "/";

  private final HttpClient httpClient;
  private final String path;

  private final HttpHeaders headers = new DefaultHttpHeaders();

  private final WebsocketClientSpec.Builder specBuilder =
      WebsocketClientSpec.builder().maxFramePayloadLength(GSocketFrameCodec.FRAME_LENGTH_MASK);

  public static WebSocketClientTransport create(HttpClient httpClient) {
    return new WebSocketClientTransport(httpClient, DEFAULT_PATH);
  }

  private WebSocketClientTransport(HttpClient httpClient, String path) {
    this.httpClient = Objects.requireNonNull(httpClient);
    Objects.requireNonNull(path);
    this.path = path.startsWith("/") ? path : "/" + path;
  }

  public WebSocketClientTransport header(String name, String... values) {
    if (values != null) {
      Arrays.stream(values).forEach(value -> headers.add(name, value));
    }
    return this;
  }

  public WebSocketClientTransport webSocketSpec(Consumer<WebsocketClientSpec.Builder> configurer) {
    configurer.accept(specBuilder);
    return this;
  }

  @Override
  public int maxFrameLength() {
    return specBuilder.build().maxFramePayloadLength();
  }

  @Override
  public Mono<? extends Connection> connect() {
    return this.httpClient
        .headers(headers -> headers.add(this.headers))
        .websocket(specBuilder.build())
        .uri(path)
        .connect();
  }
}
