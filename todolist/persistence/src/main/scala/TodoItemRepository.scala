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

package examples.todolist.persistence
import cats.~>

import examples.todolist.TodoItem

trait TodoItemRepository[F[_]] { self =>

  def insert(item: TodoItem): F[Option[TodoItem]]

  def get(id: Int): F[Option[TodoItem]]

  def delete(id: Int): F[Int]

  def update(item: TodoItem): F[Option[TodoItem]]

  def list: F[List[TodoItem]]

  def drop: F[Int]

  def create: F[Int]

  def init: F[Int]

  def mapK[G[_]](fk: F ~> G): TodoItemRepository[G] = new TodoItemRepository[G] {
    def insert(item: TodoItem): G[Option[TodoItem]] = fk(self.insert(item))

    def get(id: Int): G[Option[TodoItem]] = fk(self.get(id))

    def delete(id: Int): G[Int] = fk(self.delete(id))

    def update(input: TodoItem): G[Option[TodoItem]] = fk(self.update(input))

    def list: G[List[TodoItem]] = fk(self.list)

    def drop: G[Int] = fk(self.drop)

    def create: G[Int] = fk(self.create)

    def init: G[Int] = fk(self.init)
  }
}

object TodoItemRepository {
  def apply[F[_]](implicit F: TodoItemRepository[F]): TodoItemRepository[F] = F
}
