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

package examples.todolist.persistence.runtime

import doobie.ConnectionIO
import examples.todolist.TodoItem
import examples.todolist.persistence.TodoItemRepository

class TodoItemRepositoryHandler[F[_]] extends TodoItemRepository[ConnectionIO] {

  import examples.todolist.persistence.runtime.queries.TodoItemQueries._

  def insert(item: TodoItem): ConnectionIO[Option[TodoItem]] =
    insertQuery(item)
      .withUniqueGeneratedKeys[Int]("id")
      .flatMap(getQuery(_).option)

  def get(id: Int): ConnectionIO[Option[TodoItem]] =
    getQuery(id).option

  def update(item: TodoItem): ConnectionIO[Option[TodoItem]] =
    updateQuery(item).run
      .flatMap(_ => getQuery(item.id.get).option)

  def delete(id: Int): ConnectionIO[Int] =
    deleteQuery(id).run

  def list: ConnectionIO[List[TodoItem]] =
    listQuery
      .to[List]

  def drop: ConnectionIO[Int] =
    dropQuery.run

  def create: ConnectionIO[Int] =
    createQuery.run

  def init: ConnectionIO[Int] =
    dropQuery.run
      .flatMap(drops =>
        createQuery.run
          .map(_ + drops)
      )
}
