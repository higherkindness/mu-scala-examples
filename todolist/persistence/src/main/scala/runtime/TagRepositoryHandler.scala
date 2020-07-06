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
import examples.todolist.Tag
import examples.todolist.persistence.TagRepository

class TagRepositoryHandler[F[_]: Monad] extends TagRepository[ConnectionIO] {

  import examples.todolist.persistence.runtime.queries.TagQueries._

  override def insert(input: Tag): ConnectionIO[Option[Tag]] =
    insertQuery(input)
      .withUniqueGeneratedKeys[Int]("id")
      .flatMap(getQuery(_).option)

  override def get(id: Int): ConnectionIO[Option[Tag]] =
    getQuery(id).option

  override def update(tag: Tag): ConnectionIO[Option[Tag]] =
    updateQuery(tag).run
      .flatMap(_ => getQuery(tag.id.get).option)

  override def delete(id: Int): ConnectionIO[Int] =
    deleteQuery(id).run

  override def list: ConnectionIO[List[Tag]] =
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
