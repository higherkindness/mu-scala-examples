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

package examples.todolist.server

import cats.effect.{Blocker, ContextShift, IO, Timer}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import examples.todolist.persistence.runtime._
import examples.todolist.persistence._
import examples.todolist.protocol.Protocols._
import examples.todolist.runtime.CommonRuntime
import examples.todolist.server.handlers._
import java.util.Properties

sealed trait ServerImplicits extends RepositoriesImplicits {

  implicit val pingPongServiceHandler: PingPongService[IO] =
    new PingPongServiceHandler[IO]()

  implicit val tagRpcServiceHandler: TagRpcService[IO] =
    new TagRpcServiceHandler[IO]()

  implicit val todoListRpcServiceHandler: TodoListRpcService[IO] =
    new TodoListRpcServiceHandler[IO]()

  implicit val todoItemRpcServiceHandler: TodoItemRpcService[IO] =
    new TodoItemRpcServiceHandler[IO]()
}

sealed trait RepositoriesImplicits extends CommonRuntime {

  implicit val timer: Timer[IO]     = IO.timer(EC)
  implicit val cs: ContextShift[IO] = IO.contextShift(EC)
  implicit val bl: Blocker          = Blocker.liftExecutionContext(EC)

  private val xa: HikariTransactor[IO] =
    HikariTransactor[IO](
      new HikariDataSource(
        new HikariConfig(
          new Properties {
            setProperty("driverClassName", "org.h2.Driver")
            setProperty("jdbcUrl", "jdbc:h2:mem:todo")
            setProperty("username", "sa")
            setProperty("password", "")
            setProperty("maximumPoolSize", "10")
            setProperty("minimumIdle", "10")
            setProperty("idleTimeout", "600000")
            setProperty("connectionTimeout", "30000")
            setProperty("connectionTestQuery", "SELECT 1")
            setProperty("maxLifetime", "1800000")
            setProperty("autoCommit", "true")
          }
        )
      ),
      EC,
      bl
    )

  implicit val tagRepositoryHandler: TagRepository[IO] =
    (new TagRepositoryHandler[ConnectionIO]).mapK(xa.trans)

  implicit val todoListRepositoryHandler: TodoListRepository[IO] =
    (new TodoListRepositoryHandler[ConnectionIO]).mapK(xa.trans)

  implicit val todoItemRepositoryHandler: TodoItemRepository[IO] =
    (new TodoItemRepositoryHandler[ConnectionIO]).mapK(xa.trans)

}

object implicits extends ServerImplicits
