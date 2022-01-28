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

package higherkindness.mu.rpc.healthcheck.sFs2

import cats.effect.IO
import cats.effect.kernel.Resource
import higherkindness.mu.rpc.server.{AddService, GrpcServer}
import gserver.implicits._
import cats.syntax.traverse._
import higherkindness.mu.rpc.healthcheck.fs2.handler.HealthServiceFS2
import higherkindness.mu.rpc.healthcheck.fs2.serviceFS2.HealthCheckServiceFS2

object ServerApp {

  def main(args: Array[String]): Unit = {

    val healthCheck: IO[HealthCheckServiceFS2[IO]] = HealthServiceFS2.buildInstance[IO]

    def grpcConfigs(implicit HC: HealthCheckServiceFS2[IO]): Resource[IO, List[AddService]] =
      List(
        HealthCheckServiceFS2.bindService[IO]
      ).sequence.map(_.map(AddService))

    val server = for {
      health <- Resource.eval(healthCheck)
      config <- grpcConfigs(health)
      server <- Resource.eval(GrpcServer.default[IO](50051, config))
    } yield server

    server.use(GrpcServer.server[IO]).unsafeRunSync()
  }
}
