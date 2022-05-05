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

import cats.effect.{ExitCode, IO, IOApp, Resource}
import higherkindness.mu.rpc.{ChannelFor, ChannelForAddress}
import grpc.health.v1.health.Health
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  sealed trait Mode
  case object Check extends Mode
  case object Watch extends Mode

  case class Args(mode: Mode, serviceName: String)

  implicit val logger: SelfAwareStructuredLogger[IO] =
    Slf4jLogger.getLogger

  val channelFor: ChannelFor                = ChannelForAddress("localhost", 50051)
  val client: Resource[IO, Health[IO]]      = Health.client[IO](channelFor)
  val handler: HealthCheckClientHandler[IO] = new HealthCheckClientHandler[IO](client)

  override def run(rawArgs: List[String]): IO[ExitCode] =
    for {
      args <- parseArgs(rawArgs)
      _    <- doStuff(args)
    } yield ExitCode.Success

  def parseArgs(args: List[String]): IO[Args] = args match {
    case "check" :: Nil                => IO.pure(Args(Check, ""))
    case "check" :: serviceName :: Nil => IO.pure(Args(Check, serviceName))
    case "watch" :: Nil                => IO.pure(Args(Watch, ""))
    case "watch" :: serviceName :: Nil => IO.pure(Args(Watch, serviceName))
    case _                             => IO.raiseError(new Exception(s"Invalid arguments: $args"))
  }

  def doStuff(args: Args): IO[Unit] = args.mode match {
    case Check => handler.check(args.serviceName)
    case Watch => handler.watch(args.serviceName)
  }

}
