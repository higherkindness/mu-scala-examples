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

package higherkindness.mu.rpc.healthcheck.client

import fs2.Stream
import cats.effect.{Async, Resource}
import cats.syntax.all._
import grpc.health.v1.health.{Health, HealthCheckRequest}
import org.typelevel.log4cats.Logger

class HealthCheckClientHandler[F[_]: Async](client: Resource[F, Health[F]])(
    implicit logger: Logger[F]
) {

  def check(serviceName: String): F[Unit] =
    client.use(c =>
      c.Check(HealthCheckRequest(serviceName)).flatMap(response =>
        logger.info(s"Service '${serviceName}' has status ${response.status.name}")
      )
    )

  def watch(serviceName: String): F[Unit] =
    client.use(c =>
      Stream
        .force(c.Watch(HealthCheckRequest(serviceName)))
        .evalMap(response =>
          logger.info(s"Service '${serviceName}' was updated to status ${response.status.name}")
        )
        .compile
        .drain
    )

}
