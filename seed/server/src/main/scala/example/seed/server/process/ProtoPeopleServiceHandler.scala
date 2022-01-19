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

package example.seed.server.process

import cats.effect._
import cats.syntax.applicative._
import cats.syntax.functor._
import example.seed.protocol.proto.people._
import example.seed.protocol.proto.services.PeopleService
import fs2._
import org.typelevel.log4cats.Logger

import scala.concurrent.duration._

class ProtoPeopleServiceHandler[F[_]](implicit F: Async[F], L: Logger[F]) extends PeopleService[F] {

  val serviceName = "ProtoPeopleService"

  def getPerson(request: PeopleRequest): F[PeopleResponse] =
    L.info(s"$serviceName - Request: $request").as(PeopleResponse(Option(Person(request.name, 10))))

  def getPersonStream(request: Stream[F, PeopleRequest]): F[Stream[F, PeopleResponse]] = {

    def responseStream(person: PeopleRequest): Stream[F, PeopleResponse] = {
      val response = PeopleResponse(Option(Person(person.name, 10)))
      Stream
        .awakeEvery[F](2.seconds)
        .evalMap(_ =>
          L.info(s"$serviceName - Stream Response: $response")
            .as(response)
        )
    }

    val stream = for {
      person   <- request
      _        <- Stream.eval(L.info(s"$serviceName - Stream Request: $person"))
      response <- responseStream(person)
    } yield response

    stream.pure[F]
  }

}
