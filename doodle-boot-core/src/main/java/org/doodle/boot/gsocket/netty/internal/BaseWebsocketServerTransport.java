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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;

@Slf4j
abstract class BaseWebsocketServerTransport<SELF> implements ServerTransport {
  private static final ChannelHandler pongHandler = new PongHandler();

  static Function<HttpServer, HttpServer> serverConfigurer =
      server -> server.doOnConnection(connection -> connection.addHandlerLast(pongHandler));

  final WebsocketServerSpec.Builder specBuilder =
      WebsocketServerSpec.builder().maxFramePayloadLength(GSocketFrameCodec.FRAME_LENGTH_MASK);

  @SuppressWarnings("unchecked")
  public SELF webSocketSpec(Consumer<WebsocketServerSpec.Builder> configurer) {
    configurer.accept(specBuilder);
    return (SELF) this;
  }

  @Override
  public int maxFrameLength() {
    return specBuilder.build().maxFramePayloadLength();
  }

  @ChannelHandler.Sharable
  private static class PongHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      if (msg instanceof PongWebSocketFrame) {
        log.debug("received WebSocket Pong Frame");
        ReferenceCountUtil.safeRelease(msg);
        ctx.read();
      } else {
        ctx.fireChannelRead(msg);
      }
    }
  }
}
