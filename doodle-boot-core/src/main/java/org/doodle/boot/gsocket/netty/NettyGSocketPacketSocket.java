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

import org.doodle.boot.gsocket.messaging.GSocketPayloadCodec;
import org.doodle.boot.gsocket.netty.internal.DuplexConnection;
import org.doodle.design.messaging.PacketPayload;
import org.doodle.design.messaging.PacketSocket;

public class NettyGSocketPacketSocket implements PacketSocket {

  private final DuplexConnection connection;

  public NettyGSocketPacketSocket(DuplexConnection connection) {
    this.connection = connection;
  }

  @Override
  public void send(PacketPayload payload) {
    this.connection.sendFrame(
        GSocketPayloadCodec.encode(connection.alloc(), payload.metadata(), payload.data()));
  }

  @Override
  public NettyGSocketPacketSocket onReadIdle(long idleTimeout, Runnable onReadIdle) {
    return this;
  }

  @Override
  public void dispose() {}
}
