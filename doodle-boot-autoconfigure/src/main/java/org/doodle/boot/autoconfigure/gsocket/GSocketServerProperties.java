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
package org.doodle.boot.autoconfigure.gsocket;

import java.net.InetAddress;
import lombok.Data;
import org.doodle.boot.gsocket.GSocketServer;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = GSocketServerProperties.PREFIX)
public class GSocketServerProperties {
  public static final String PREFIX = GSocketProperties.PREFIX + ".server";

  private Integer port;

  private InetAddress address;

  private GSocketServer.Transport transport = GSocketServer.Transport.WEBSOCKET;
}
