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
import higherkindness.mu.rpc._
import higherkindness.mu.rpc.internal.tracing.implicits.clientContext
import fs2.Stream
import natchez._

object Client extends IOApp {

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] = {
    import natchez.jaeger.Jaeger
    import io.jaegertracing.Configuration.{
      SamplerConfiguration,
      ReporterConfiguration,
      SenderConfiguration
    }
    Jaeger.entryPoint[F]("my-client-application") { config =>
      Sync[F].delay {
        config
          .withSampler(new SamplerConfiguration().withType("const").withParam(1))
          .withReporter(
            new ReporterConfiguration()
              .withSender(
                new SenderConfiguration().withEndpoint("http://localhost:14268/api/traces")
              )
          )
          .getTracer
      }
    }
  }

  val channelFor: ChannelFor = ChannelForAddress("localhost", 12345)

  val serviceClient: Resource[IO, Greeter[Kleisli[IO, Span[IO], *]]] =
    Greeter.contextClient[IO, Span[IO]](channelFor)

  val entrypointResource: Resource[IO, EntryPoint[IO]] = entryPoint

  def run(args: List[String]): IO[ExitCode] =
    for {
      _    <- IO.println("Please enter your name: ")
      name <- IO.readLine
      response <- serviceClient.use { client =>
        entrypointResource.use { ep =>
          // Send a few requests just to warm up the JVM.
          // The traces for the first couple of requests will look really slow.
          sendUnaryRequest(client, ep, name) >>
            sendUnaryRequest(client, ep, name) >>
            sendUnaryRequest(client, ep, name) >>
            sendUnaryRequest(client, ep, name) >>
            sendUnaryRequest(client, ep, name)

          // To try a client-streaming call, comment out the lines above
          // and uncomment the line below

          // sendClientStreamingRequest(client, ep, name)

          // Or try a server-streaming call:
          // sendServerStreamingRequest(client, ep, name)

          // Or a bidirectional streaming call:
          // sendBidirectionalStreamingRequest(client, ep, name)
        }
      }
      serverMood = if (response.happy) "happy" else "unhappy"
      _ <- IO.println(s"The $serverMood server says '${response.greeting}'")
    } yield ExitCode.Success

  def sendUnaryRequest(
      client: Greeter[Kleisli[IO, Span[IO], *]],
      ep: EntryPoint[IO],
      name: String
  ): IO[HelloResponse] =
    ep.root("Client application root span (unary call)").use { span =>
      client.SayHello(HelloRequest(name)).run(span)
    }

  def sendClientStreamingRequest(
      client: Greeter[Kleisli[IO, Span[IO], *]],
      ep: EntryPoint[IO],
      name: String
  ): IO[HelloResponse] = {
    val stream = Stream[Kleisli[IO, Span[IO], *], HelloRequest](
      HelloRequest(name),
      HelloRequest(name)
    )

    ep.root("Client application root span (client-streaming call)").use { span =>
      client.ClientStreaming(stream).run(span)
    }
  }

  def sendServerStreamingRequest(
      client: Greeter[Kleisli[IO, Span[IO], *]],
      ep: EntryPoint[IO],
      name: String
  ): IO[HelloResponse] =
    ep.root("Client application root span (server-streaming call)").use { span =>
      client
        .ServerStreaming(HelloRequest(name))
        .run(span)
        .flatMap(_.compile.lastOrError.run(span))
    }

  def sendBidirectionalStreamingRequest(
      client: Greeter[Kleisli[IO, Span[IO], *]],
      ep: EntryPoint[IO],
      name: String
  ): IO[HelloResponse] = {
    val stream = Stream[Kleisli[IO, Span[IO], *], HelloRequest](
      HelloRequest(name),
      HelloRequest(name)
    )

    ep.root("Client application root span (bidirectional-streaming call)").use { span =>
      client
        .BidirectionalStreaming(stream)
        .run(span)
        .flatMap(_.compile.lastOrError.run(span))
    }
  }

}
