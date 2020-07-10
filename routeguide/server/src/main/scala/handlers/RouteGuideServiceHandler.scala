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

package example.routeguide.server.handlers

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import cats.syntax.applicative._
import cats.effect.{Async, ConcurrentEffect, Effect}
import example.routeguide.protocol.service._
import fs2.Stream
import example.routeguide.common.Utils._
import monix.eval.Task
import monix.reactive.Observable
import org.log4s._

import scala.concurrent.duration.NANOSECONDS

class RouteGuideServiceHandler[F[_]: ConcurrentEffect](implicit E: Effect[Task])
    extends RouteGuideService[F] {

  // AtomicReference as an alternative to ConcurrentMap<Point, List<RouteNote>>?
  private val routeNotes: AtomicReference[Map[Point, List[RouteNote]]] =
    new AtomicReference[Map[Point, List[RouteNote]]](Map.empty)

  val logger = getLogger

  override def getFeature(point: Point): F[Feature] =
    Async[F].delay {
      logger.info(s"Fetching feature at ${point.pretty} ...")
      point.findFeatureIn(features)
    }

  override def listFeatures(rectangle: Rectangle): F[Stream[Feature]] = {
    val left   = Math.min(rectangle.lo.get.longitude, rectangle.hi.get.longitude)
    val right  = Math.max(rectangle.lo.get.longitude, rectangle.hi.get.longitude)
    val top    = Math.max(rectangle.lo.get.latitude, rectangle.hi.get.latitude)
    val bottom = Math.min(rectangle.lo.get.latitude, rectangle.hi.get.latitude)

    val observable = Stream.fromIterable(
      features.filter { feature =>
        val lat = feature.location.get.latitude
        val lon = feature.location.get.longitude
        feature.valid && lon >= left && lon <= right && lat >= bottom && lat <= top

      }
    )

    logger.info(s"Listing features for $rectangle ...")

    observable.pure[F]
  }

  override def recordRoute(points: Observable[Point]): F[RouteSummary] =
    // For each point after the first, add the incremental distance from the previous point to
    // the total distance value. We're starting

    // We have to applyApplies a binary operator to a start value and all elements of
    // the source, going left to right and returns a new `Task` that
    // upon evaluation will eventually emit the final result.
    points
      .foldLeftL((RouteSummary(0, 0, 0, 0), None: Option[Point], System.nanoTime())) {
        case ((summary, previous, startTime), point) =>
          val feature  = point.findFeatureIn(features)
          val distance = previous.map(calcDistance(_, point)) getOrElse 0
          val updated = summary.copy(
            point_count = summary.point_count + 1,
            feature_count = summary.feature_count + (if (feature.valid) 1 else 0),
            distance = summary.distance + distance,
            elapsed_time = NANOSECONDS.toSeconds(System.nanoTime() - startTime).toInt
          )
          (updated, Some(point), startTime)
      }
      .map(_._1)
      .toAsync[F]

  override def routeChat(routeNotes: Observable[RouteNote]): F[Observable[RouteNote]] =
    routeNotes
      .flatMap { note: RouteNote =>
        logger.info(s"Got route note $note, adding it... ")

        addNote(note)
        Observable.fromIterable(getOrCreateNotes(note.location.get))
      }
      .onErrorHandle { e =>
        logger.warn(s"routeChat cancelled $e")
        throw e
      }
      .pure[F]

  private[this] def addNote(note: RouteNote): Map[Point, List[RouteNote]] =
    routeNotes.updateAndGet(new UnaryOperator[Map[Point, List[RouteNote]]] {
      override def apply(notes: Map[Point, List[RouteNote]]): Map[Point, List[RouteNote]] = {
        val newRouteNotes = notes.getOrElse(note.location.get, Nil) :+ note
        notes + (note.location.get -> newRouteNotes)
      }
    })

  private[this] def getOrCreateNotes(point: Point): List[RouteNote] =
    routeNotes.get.getOrElse(point, Nil)
}
