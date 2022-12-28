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
package org.doodle.boot.gsocket.context;

import lombok.Setter;
import org.doodle.boot.gsocket.GSocketServer;
import org.doodle.boot.gsocket.GSocketServerFactory;
import org.doodle.boot.gsocket.netty.internal.ServerTransport;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;

public class GSocketServerBootstrap implements ApplicationEventPublisherAware, SmartLifecycle {

  @Setter private ApplicationEventPublisher applicationEventPublisher;

  private final GSocketServer server;

  public GSocketServerBootstrap(
      GSocketServerFactory serverFactory, ServerTransport.ConnectionAcceptor acceptor) {
    this.server = serverFactory.create(acceptor);
  }

  @Override
  public void start() {
    this.server.start();
    this.applicationEventPublisher.publishEvent(new GSocketServerInitializedEvent(this.server));
  }

  @Override
  public void stop() {
    this.server.stop();
  }

  @Override
  public boolean isRunning() {
    GSocketServer server = this.server;
    if (server != null) {
      return server.address() != null;
    } else {
      return false;
    }
  }
}
