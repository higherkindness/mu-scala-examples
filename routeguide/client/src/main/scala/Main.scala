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

package example.routeguide.client

import cats.effect.IO
import org.log4s._
import example.routeguide.client.implicits._
import example.routeguide.client.ClientProgram._

object Main {

  val logger = getLogger

  def main(args: Array[String]): Unit = {
    logger.info(s"${Thread.currentThread().getName} Starting client, interpreting to Future ...")

    clientProgram[IO].unsafeRunSync()

    logger.info(s"${Thread.currentThread().getName} Finishing program interpretation ...")
  }

}
