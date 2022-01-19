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
import com.example.happy._
import higherkindness.mu.rpc.server._
import natchez._

object ServerB extends IOApp {

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] = {
    import natchez.jaeger.Jaeger
    import io.jaegertracing.Configuration.SamplerConfiguration
    import io.jaegertracing.Configuration.ReporterConfiguration
    Jaeger.entryPoint[F]("ServiceB") { c =>
      Sync[F].delay {
        c.withSampler(new SamplerConfiguration().withType("const").withParam(1))
          .withReporter(ReporterConfiguration.fromEnv)
          .getTracer
      }
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    entryPoint[IO].use { ep =>
      implicit val service: HappinessService[Kleisli[IO, Span[IO], *]] =
        new MyHappinessService[Kleisli[IO, Span[IO], *]]
      HappinessService.bindTracingService[IO](ep).use { serviceDef =>
        for {
          server <- GrpcServer.default[IO](12346, List(AddService(serviceDef)))
          _      <- GrpcServer.server[IO](server)
        } yield ExitCode.Success
      }
    }
  }

}
