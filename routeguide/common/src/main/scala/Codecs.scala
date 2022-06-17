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

package example.routeguide.common

import io.circe._
import example.routeguide.protocol.service._

object Codecs {

  implicit val pointCodec: Codec[Point] = Codec.forProduct2[Point, Int, Int](
    "latitude",
    "longitude"
  )((lat, lon) => Point(lat, lon))(p => (p.latitude, p.longitude))

  implicit val featureCodec: Codec[Feature] = Codec.forProduct2[Feature, String, Option[Point]](
    "name",
    "location"
  )((name, loc) => Feature(name, loc))(f => (f.name, f.location))

  implicit val featureDBCodec: Codec[FeatureDatabase] =
    Codec.forProduct1[FeatureDatabase, Seq[Feature]](
      "feature"
    )(features => FeatureDatabase(features))(
      _.feature
    )

}
