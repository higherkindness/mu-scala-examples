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

import cats.Monad
import doobie.ConnectionIO
import examples.todolist.TodoList
import examples.todolist.persistence.TodoListRepository

class TodoListRepositoryHandler[F[_]: Monad] extends TodoListRepository[ConnectionIO] {

  import examples.todolist.persistence.runtime.queries.TodoListQueries._

  override def insert(item: TodoList): ConnectionIO[Option[TodoList]] =
    insertQuery(item)
      .withUniqueGeneratedKeys[Int]("id")
      .flatMap(getQuery(_).option)

  override def get(id: Int): ConnectionIO[Option[TodoList]] =
    getQuery(id).option

  override def update(input: TodoList): ConnectionIO[Option[TodoList]] =
    updateQuery(input).run
      .flatMap(_ => getQuery(input.id.get).option)

  override def delete(id: Int): ConnectionIO[Int] =
    deleteQuery(id).run

  override def list: ConnectionIO[List[TodoList]] =
    listQuery
      .to[List]

  override def drop: ConnectionIO[Int] =
    dropQuery.run

  override def create: ConnectionIO[Int] =
    createQuery.run

  override def init: ConnectionIO[Int] =
    dropQuery.run
      .flatMap(drops =>
        createQuery.run
          .map(_ + drops)
      )
}
