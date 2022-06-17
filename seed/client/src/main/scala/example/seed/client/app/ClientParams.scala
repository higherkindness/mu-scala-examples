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

package example.seed.client.app

import example.seed.client.common.models.ParamsConfig
import scopt.OptionParser

object ClientParams {

  val default = ParamsConfig("Foo")

  def paramsConfig(name: String): OptionParser[ParamsConfig] =
    new scopt.OptionParser[ParamsConfig](name) {

      opt[String]("name")
        .optional()
        .action((value, config) => config.copy(request = value))
        .text("The name for the request")

    }

  def loadParams(name: String, args: List[String]): ParamsConfig =
    paramsConfig(name).parse(args, default).getOrElse(default)

}
