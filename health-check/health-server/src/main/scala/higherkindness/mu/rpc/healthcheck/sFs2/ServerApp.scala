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

package higherkindness.mu.rpc.healthcheck.server

import cats.effect.{IO, IOApp, Resource}
import cats.effect.std.Random
import fs2._
import higherkindness.mu.rpc.healthcheck.{HealthService, ServiceStatus}
import higherkindness.mu.rpc.server.{AddService, GrpcServer}
import _root_.grpc.health.v1.health.Health
import _root_.grpc.health.v1.health.HealthCheckResponse.ServingStatus
import scala.concurrent.duration._

object ServerApp extends IOApp.Simple {

  def run: IO[Unit] = {

    val healthCheck: IO[HealthService[IO]] = HealthService.build[IO]

    def grpcConfigs(implicit HC: Health[IO]): Resource[IO, List[AddService]] =
      Health.bindService[IO].map(serviceDef => List(AddService(serviceDef)))

    def randomStatus(random: Random[IO]): IO[ServingStatus] =
      random.nextBoolean.map {
        case true => ServingStatus.SERVING
        case false => ServingStatus.NOT_SERVING
      }

    // A stream of random updates to the status of service "A"
    def randomStatusUpdates(service: HealthService[IO]): Stream[IO, Unit] =
      Stream.eval(Random.scalaUtilRandom[IO]).flatMap { random =>
        Stream
          .fixedRate[IO](1.second)
          .evalMap(_ =>
            randomStatus(random)
              .flatMap(status => service.setStatus(ServiceStatus("A", status)))
          )
      }

    val service = for {
      health <- Resource.eval(healthCheck)
      config <- grpcConfigs(health)
      _      <- GrpcServer.defaultServer[IO](50051, config)
    } yield health

    // This will start the server and run forever,
    // randomly updating the status of service "A"
    service.use(s => randomStatusUpdates(s).compile.drain)
  }
}
