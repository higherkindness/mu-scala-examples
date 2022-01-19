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
import examples.todolist.protocol._
import examples.todolist.protocol.Protocols._
import examples.todolist.Tag
import examples.todolist.persistence.TagRepository
import higherkindness.mu.rpc.protocol.Empty
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class TagRpcServiceHandler[F[_]: Sync](implicit repo: TagRepository[F]) extends TagRpcService[F] {

  import TagConversions._

  val L: Logger[F]  = Slf4jLogger.getLogger[F]
  val model: String = classOf[Tag].getSimpleName

  override def reset(empty: Empty.type): F[MessageId] =
    for {
      _     <- L.debug(s"Trying to reset $model in repository")
      items <- repo.init
      _     <- L.warn(s"Reset $model table in repository")
    } yield MessageId(items)

  override def insert(tagRequest: TagRequest): F[TagResponse] =
    for {
      _            <- L.debug(s"Trying to insert a $model")
      insertedItem <- repo.insert(tagRequest.toTag)
      _            <- L.info(s"Tried to add $model")
    } yield TagResponse(insertedItem.flatMap(_.toTagMessage))

  override def retrieve(id: MessageId): F[TagResponse] =
    for {
      _    <- L.debug(s"Trying to retrieve a $model")
      item <- repo.get(id.value)
      _    <- L.info(s"Found $model: $item")
    } yield TagResponse(item.flatMap(_.toTagMessage))

  override def list(empty: Empty.type): F[TagList] =
    for {
      _     <- L.debug(s"Trying to get all $model models")
      items <- repo.list
      _     <- L.info(s"Found all $model models")
    } yield TagList(items.flatMap(_.toTagMessage))

  override def update(tag: TagMessage): F[TagResponse] =
    for {
      _           <- L.debug(s"Trying to update a $model")
      updatedItem <- repo.update(tag.toTag)
      _           <- L.info(s"Tried to update $model")
    } yield TagResponse(updatedItem.flatMap(_.toTagMessage))

  override def destroy(id: MessageId): F[MessageId] =
    for {
      _            <- L.debug(s"Trying to delete a $model")
      deletedItems <- repo.delete(id.value)
      _            <- L.info(s"Tried to delete $model")
    } yield MessageId(deletedItems)
}

object TagConversions {

  implicit class TagRequestToTag(tr: TagRequest) {
    def toTag: Tag = Tag(tr.name)
  }

  implicit class TagMessageToTag(t: TagMessage) {
    def toTag: Tag = Tag(t.name, Option(t.id))
  }

  implicit class TagToTagMessage(t: Tag) {
    def toTagMessage: Option[TagMessage] =
      t.id.map(TagMessage(t.name, _))
  }
}
