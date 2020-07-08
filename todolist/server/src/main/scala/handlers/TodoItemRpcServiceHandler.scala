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
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.option._
import examples.todolist.TodoItem
import examples.todolist.persistence.TodoItemRepository
import examples.todolist.protocol.Protocols._
import examples.todolist.protocol._
import higherkindness.mu.rpc.protocol.Empty
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class TodoItemRpcServiceHandler[F[_]: Sync](implicit repo: TodoItemRepository[F])
    extends TodoItemRpcService[F] {

  import TodoItemConversions._

  val L: Logger[F]  = Slf4jLogger.getLogger[F]
  val model: String = classOf[TodoItem].getSimpleName

  override def reset(empty: Empty.type): F[MessageId] =
    for {
      _   <- L.debug(s"Trying to reset $model in repository")
      ops <- repo.init
      _   <- L.warn(s"Reset $model table in repository")
    } yield MessageId(ops)

  override def insert(item: TodoItemRequest): F[TodoItemResponse] =
    for {
      _            <- L.debug(s"Trying to insert a $model")
      insertedItem <- repo.insert(item.toTodoItem)
      _            <- L.info(s"$model inserted")
    } yield TodoItemResponse(insertedItem.flatMap(_.toTodoItemMessage))

  override def retrieve(id: MessageId): F[TodoItemResponse] =
    for {
      _    <- L.debug(s"Trying to retrieve a $model")
      item <- repo.get(id.value)
      _    <- L.info(s"Found ${item}")
    } yield TodoItemResponse(item.flatMap(_.toTodoItemMessage))

  override def list(empty: Empty.type): F[TodoItemList] =
    for {
      _     <- L.debug(s"Trying to get all $model models")
      items <- repo.list
      _     <- L.info(s"Found all $model models")
    } yield TodoItemList(items.flatMap(_.toTodoItemMessage))

  override def update(item: TodoItemMessage): F[TodoItemResponse] =
    for {
      _           <- L.debug(s"Trying to update a $model")
      updatedItem <- repo.update(item.toTodoItem)
      _           <- L.info(s"Tried to update a $model")
    } yield TodoItemResponse(updatedItem.flatMap(_.toTodoItemMessage))

  override def destroy(id: MessageId): F[MessageId] =
    for {
      _           <- L.debug(s"Trying to destroy a $model")
      deletedItem <- repo.delete(id.value)
      _           <- L.info(s"Tried to delete $model")
    } yield MessageId(deletedItem)

}

object TodoItemConversions {

  implicit class TodoItemRequestToTodoItem(it: TodoItemRequest) {
    def toTodoItem: TodoItem =
      TodoItem(item = it.item, todoListId = it.todoListId.some, completed = false, id = None)
  }

  implicit class TodoItemToTodoItemMessage(it: TodoItem) {
    def toTodoItemMessage: Option[TodoItemMessage] =
      for {
        id         <- it.id
        todoListId <- it.todoListId
      } yield TodoItemMessage(it.item, id, it.completed, todoListId)
  }

  implicit class TodoItemMessageToTodoItem(it: TodoItemMessage) {
    def toTodoItem: TodoItem = TodoItem(it.item, it.id.some, it.completed, it.todoListId.some)
  }

}
