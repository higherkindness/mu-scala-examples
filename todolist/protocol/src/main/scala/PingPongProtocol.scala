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

package examples.todolist
package protocol

import higherkindness.mu.rpc.protocol._

trait PingPongProtocol {

  /**
   * Pong response with current timestamp
   *
   * @param time
   *   Current timestamp.
   */
  case class Pong(time: Long = System.currentTimeMillis() / 1000L)

  @service(Protobuf)
  trait PingPongService[F[_]] {

    /**
     * A simple ping-pong rpc.
     *
     * @param empty
     * @return
     *   Pong response with current timestamp.
     */
    def ping(empty: Empty.type): F[Pong]

  }
}
