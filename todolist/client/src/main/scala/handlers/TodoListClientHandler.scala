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

package examples.todolist.client
package handlers

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.{Resource, Sync}
import examples.todolist.client.clients.TodoListClient
import examples.todolist.protocol.Protocols._
import examples.todolist.protocol._
import higherkindness.mu.rpc.protocol.Empty
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class TodoListClientHandler[F[_]: Sync](client: Resource[F, TodoListRpcService[F]])
    extends TodoListClient[F] {

  val logger: Logger[F] = Slf4jLogger.getLogger[F]

  override def reset(): F[Int] =
    for {
      _ <- logger.debug(s"Calling to restart todo list data")
      r <- client.use(_.reset(Empty))
    } yield r.value

  override def insert(request: TodoListRequest): F[Option[TodoListMessage]] =
    for {
      _ <- logger.debug(
        s"Calling to insert todo list with name: ${request.title} and id: ${request.tagId}"
      )
      t <- client.use(_.insert(request))
    } yield t.msg

  override def retrieve(id: Int): F[Option[TodoListMessage]] =
    for {
      _ <- logger.debug(s"Calling to get todo list with id: $id")
      r <- client.use(_.retrieve(MessageId(id)))
    } yield r.msg

  override def list(): F[TodoListList] =
    for {
      _ <- logger.debug(s"Calling to get all todo lists")
      r <- client.use(_.list(Empty))
    } yield r

  override def update(todoList: TodoListMessage): F[Option[TodoListMessage]] =
    for {
      _ <- logger.debug(
        s"Calling to update todo list with title: ${todoList.title}, tagId: ${todoList.tagId} and id: ${todoList.id}"
      )
      r <- client.use(_.update(todoList))
    } yield r.msg

  override def remove(id: Int): F[Int] =
    for {
      _ <- logger.debug(s"Calling to delete tag with id: $id")
      r <- client.use(_.destroy(MessageId(id)))
    } yield r.value
}
