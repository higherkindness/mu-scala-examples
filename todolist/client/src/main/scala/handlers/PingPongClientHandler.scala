/*
 * Copyright 2020 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package examples.todolist.client
package handlers

import cats.syntax.flatMap._
import cats.effect.{Resource, Sync}
import examples.todolist.client.clients.PingPongClient
import examples.todolist.protocol.Protocols.PingPongService
import higherkindness.mu.rpc.protocol.Empty
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class PingPongClientHandler[F[_]: Sync](client: Resource[F, PingPongService[F]])
    extends PingPongClient[F] {

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  override def ping(): F[Unit] =
    client
      .use(_.ping(Empty))
      .flatMap(p => logger.info(s"Pong received with timestamp: ${p.time}"))
}
