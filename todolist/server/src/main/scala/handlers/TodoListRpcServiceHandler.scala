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
package handlers

import cats.effect.Sync
import cats.syntax.option._
import examples.todolist.protocol._
import examples.todolist.protocol.Protocols._
import examples.todolist.TodoList
import examples.todolist.persistence.TodoListRepository
import higherkindness.mu.rpc.protocol.Empty
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class TodoListRpcServiceHandler[F[_]: Sync] extends TodoListRpcService[F] {

  import TodoListConversions._

  val L: Logger[F] = Slf4jLogger.getLogger[F]
  val repo: TodoListRepository[F]
  val model: String = classOf[TodoList].getSimpleName

  override def reset(empty: Empty.type): F[MessageId] =
    for {
      _   <- L.debug(s"Trying to reset $model in repository")
      ops <- repo.init
      _   <- L.warn(s"Reset $model table in repository")
    } yield ops.map(MessageId)

  override def insert(item: TodoListRequest): F[TodoListResponse] =
    for {
      _            <- L.debug(s"Trying to insert a $model")
      insertedItem <- repo.insert(item.toTodoList)
      _            <- L.info(s"Tried to add a $model")
    } yield insertedItem.map(_.toTodoList)

  override def retrieve(id: MessageId): F[TodoListResponse] =
    for {
      _    <- L.debug(s"Trying to retrieve a $model")
      item <- repo.get(id.value)
      _    <- L.info(s"Found ${item}")
    } yield item.map(_.toTodoList)

  override def list(empty: Empty.type): F[TodoListList] =
    for {
      _     <- L.debug(s"Trying to get all $model models")
      items <- repo.list
      _     <- L.info(s"Found all $model models")
    } yield items.map(_.flatMap(_.toTodoListMessage)).map(TodoListList)

  override def update(item: TodoListMessage): F[TodoListResponse] =
    for {
      _           <- L.debug(s"Trying to update a $model")
      updatedItem <- repo.update(item.toTodoList)
      _           <- L.info(s"Tried to update a $model")
    } yield updatedItem.map(_.toTodoList)

  override def destroy(id: MessageId): F[MessageId] =
    for {
      _           <- L.debug(s"Trying to destroy a $model")
      deletedItem <- repo.delete(id.value)
      _           <- L.info(s"Tried to delete $model")
    } yield deletedItem.map(MessageId)
}

object TodoListConversions {

  implicit class TodoListRequestToTodoList(tr: TodoListRequest) {
    def toTodoList: TodoList = TodoList(tr.title, tr.tagId.some, None)
  }

  implicit class TodoListToTodoListMessage(tl: TodoList) {
    def toTodoListMessage: Option[TodoListMessage] =
      for {
        id    <- tl.id
        tagid <- tl.tagId
      } yield TodoListMessage(tl.title, id, tagid)
  }

  implicit class TodoListMessageToTodoList(tm: TodoListMessage) {
    def toTodoList: TodoList = TodoList(tm.title, tm.tagId.some, tm.id.some)
  }

  implicit class OptionTodoListTodoListResponse(ol: Option[TodoList]) {

    def toTodoList: TodoListResponse =
      TodoListResponse(ol.flatMap(_.toTodoListMessage))
  }
}
