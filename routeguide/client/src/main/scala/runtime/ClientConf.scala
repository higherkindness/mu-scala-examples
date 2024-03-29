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

package example.routeguide.client.runtime

import cats.effect.IO
import higherkindness.mu.rpc.ChannelFor
import higherkindness.mu.rpc.config.channel.ConfigForAddress

trait ClientConf {

  val channelFor: ChannelFor =
    ConfigForAddress[IO]("rpc.client.host", "rpc.client.port").unsafeRunSync()(
      cats.effect.unsafe.IORuntime.global
    )

}
