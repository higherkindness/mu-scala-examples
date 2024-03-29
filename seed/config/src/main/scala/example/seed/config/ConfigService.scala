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

package example.seed.config

import cats.effect._
import cats.syntax.either._
import pureconfig.ConfigReader
import pureconfig.ConfigSource

trait ConfigService[F[_]] {

  def serviceConfig[Config: ConfigReader]: F[Config]

}

object ConfigService {
  def apply[F[_]: Async]: ConfigService[F] = new ConfigService[F] {

    override def serviceConfig[Config: ConfigReader]: F[Config] =
      Async[F].fromEither(
        ConfigSource.default
          .load[Config]
          .leftMap(e => new IllegalStateException(s"Error loading configuration: $e"))
      )

  }
}
