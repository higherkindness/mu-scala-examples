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

package example.seed.client.process.runtime

import cats.syntax.either._
import example.seed.client.common.models.PeopleError
import example.seed.protocol.avro._
import shapeless.Poly1

object handlers {

  object PeopleResponseLogger extends Poly1 {
    implicit val peh1: PeopleResponseLogger.Case.Aux[NotFoundError, String] =
      at[NotFoundError](_.message)
    implicit val peh2: PeopleResponseLogger.Case.Aux[DuplicatedPersonError, String] =
      at[DuplicatedPersonError](_.message)
    implicit val peh3: PeopleResponseLogger.Case.Aux[Person, String] =
      at[Person](_.toString)
  }

  object PeopleResponseHandler extends Poly1 {
    implicit val peh1: PeopleResponseHandler.Case.Aux[NotFoundError, Either[PeopleError, Person]] =
      at[NotFoundError](e => PeopleError(e.message).asLeft[Person])
    implicit val peh2: PeopleResponseHandler.Case.Aux[DuplicatedPersonError, Either[
      PeopleError,
      Person
    ]] =
      at[DuplicatedPersonError](e => PeopleError(e.message).asLeft[Person])
    implicit val peh3: PeopleResponseHandler.Case.Aux[Person, Either[PeopleError, Person]] =
      at[Person](_.asRight[PeopleError])
  }
}
