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

package com.example

import cats.data.Kleisli
import cats.effect._
import com.example.hello._
import com.example.happy._
import higherkindness.mu.rpc._
import higherkindness.mu.rpc.internal.tracing.implicits._
import higherkindness.mu.rpc.server._
import natchez._

object ServerA extends IOApp {

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] = {
    import natchez.jaeger.Jaeger
    import io.jaegertracing.Configuration.{SamplerConfiguration, ReporterConfiguration, SenderConfiguration}
    Jaeger.entryPoint[F]("ServiceA") { config =>
      Sync[F].delay {
        config
          .withSampler(new SamplerConfiguration().withType("const").withParam(1))
          .withReporter(
            new ReporterConfiguration()
              .withSender(
                new SenderConfiguration().withEndpoint("http://localhost:14268/api/traces"
              )
            )
          )
          .getTracer
      }
    }
  }

  val channelFor: ChannelFor = ChannelForAddress("localhost", 12346)

  val happinessServiceClient: Resource[IO, HappinessService[Kleisli[IO, Span[IO], *]]] =
    HappinessService.contextClient[IO, Span[IO]](channelFor)

  def run(args: List[String]): IO[ExitCode] = {
    entryPoint[IO].use { implicit ep =>
      happinessServiceClient.use { client =>
        implicit val greeter: Greeter[Kleisli[IO, Span[IO], *]] =
          new MyGreeter[Kleisli[IO, Span[IO], *]](client)
        Greeter.bindContextService[IO, Span[IO]].use { serviceDef =>
          for {
            server <- GrpcServer.default[IO](12345, List(AddService(serviceDef)))
            _      <- GrpcServer.server[IO](server)
          } yield ExitCode.Success
        }
      }
    }
  }

}
