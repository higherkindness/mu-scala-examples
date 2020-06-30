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

import cats.syntax.functor._
import cats.effect.{Resource, Sync}
import examples.todolist.client.clients.PingPongClient
import examples.todolist.protocol.Protocols.PingPongService
import higherkindness.mu.rpc.protocol.Empty
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class PingPongClientHandler[F[_]: Sync](client: Resource[F, PingPongService[F]])
    extends PingPongClient[F] {

  implicit def unsafeLogger[L[_]: Sync] = Slf4jLogger.getLogger[F]

  override def ping(): F[Unit] =
    client
      .use(_.ping(Empty))
      // compiler complains about a discarded non-unit value below, which makes me think:
      // since this method isn't pure is it even worth
      // using log4cats?  The only benefit is moving from two logging libs to one
      // for this module, which seems like a benefit to me
      // I also don't understand why Logger[F].info doesn't return unit...
      .map(p => Logger[F].info(s"Pong received with timestamp: ${p.time}"))
}
