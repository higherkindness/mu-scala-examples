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

package example.seed.client.process

import java.net.InetAddress

import cats.effect._
import cats.syntax.all._
import example.seed.protocol.proto.people._
import example.seed.protocol.proto.services._
import fs2._
import higherkindness.mu.rpc.ChannelForAddress
import higherkindness.mu.rpc.channel.{ManagedChannelInterpreter, UsePlaintext}
import org.typelevel.log4cats.Logger
import io.grpc.{CallOptions, ManagedChannel}
import example.seed.client.common.PersonNotFoundError

import scala.util.Random
import scala.concurrent.duration._

trait ProtoPeopleServiceClient[F[_]] {

  def getPerson(name: String): F[Person]

  def getRandomPersonStream: Stream[F, Person]

}
object ProtoPeopleServiceClient {

  def apply[F[_]](
      client: PeopleService[F]
  )(implicit F: Async[F], L: Logger[F]): ProtoPeopleServiceClient[F] =
    new ProtoPeopleServiceClient[F] {

      val serviceName = "ProtoPeopleClient"

      def getPerson(name: String): F[Person] =
        for {
          _      <- L.info(s"")
          result <- client.getPerson(PeopleRequest(name))
          _      <- L.info(s"$serviceName - Request: $name - Result: $result")
        } yield result.person.getOrElse(throw new PersonNotFoundError("location not found"))

      def getRandomPersonStream: Stream[F, Person] = {

        def requestStream: Stream[F, PeopleRequest] =
          Stream.iterateEval(PeopleRequest("")) { _ =>
            val req = PeopleRequest(Random.nextPrintableChar().toString)
            F.sleep(2.seconds) *> L.info(s"$serviceName Stream Request: $req").as(req)
          }

        for {
          result <- Stream.force(client.getPersonStream(requestStream))
          _      <- Stream.eval(L.info(s"$serviceName Stream Result: $result"))
        } yield result.person.getOrElse(throw new PersonNotFoundError("location not found"))
      }

    }

  def createClient[F[_]: Async: Logger](
      hostname: String,
      port: Int
  ): fs2.Stream[F, ProtoPeopleServiceClient[F]] = {

    val channel: F[ManagedChannel] =
      Async[F].delay(InetAddress.getByName(hostname).getHostAddress).flatMap { ip =>
        val channelFor = ChannelForAddress(ip, port)
        new ManagedChannelInterpreter[F](channelFor, List(UsePlaintext())).build
      }

    def clientFromChannel: Resource[F, PeopleService[F]] =
      PeopleService.clientFromChannel(channel, CallOptions.DEFAULT)

    fs2.Stream.resource(clientFromChannel).map(ProtoPeopleServiceClient(_))
  }

}
