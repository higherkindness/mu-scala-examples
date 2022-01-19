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

package com.example

import fs2._
import cats.implicits._
import cats.effect.Async
import com.example.hello._
import com.example.happy._
import natchez._
import scala.concurrent.duration._

class MyGreeter[F[_]: Async: Trace](happinessClient: HappinessService[F]) extends Greeter[F] {

  def ClientStreaming(req: Stream[F, HelloRequest]): F[HelloResponse] =
    for {
      lastReq       <- req.compile.lastOrError
      happinessResp <- happinessClient.CheckHappiness(HappinessRequest())
    } yield HelloResponse(s"Hello, streaming ${lastReq.name}!", happinessResp.happy)

  def ServerStreaming(req: HelloRequest): F[Stream[F, HelloResponse]] =
    happinessClient.CheckHappiness(HappinessRequest()).map { happinessResp =>
      Stream(
        HelloResponse(s"Hello, ${req.name}!", happinessResp.happy),
        HelloResponse(s"Hi again, ${req.name}!", happinessResp.happy)
      ).covary[F]
    }

  def BidirectionalStreaming(req: Stream[F, HelloRequest]): F[Stream[F, HelloResponse]] =
    happinessClient.CheckHappiness(HappinessRequest()).map { happinessResp =>
      req.map(r => HelloResponse(s"Hello, bidirectional ${r.name}!", happinessResp.happy))
    }

  def SayHello(req: HelloRequest): F[HelloResponse] =
    for {
      cachedGreeting <- lookupGreetingInCache(req.name)
      greeting       <- cachedGreeting.fold(lookupGreetingInDBAndWriteToCache(req.name))(_.pure[F])
      happinessResp  <- happinessClient.CheckHappiness(HappinessRequest())
    } yield HelloResponse(greeting, happinessResp.happy)

  def lookupGreetingInCache(name: String): F[Option[String]] =
    Trace[F].span("lookup greeting in cache") {
      // simulate looking in Redis and not finding anything
      Async[F].sleep(5.millis) *>
        Trace[F].put(
          "name"      -> TraceValue.StringValue(name),
          "cache_hit" -> TraceValue.BooleanValue(false)
        ) *>
        none[String].pure[F]
    }

  def lookupGreetingInDB(name: String): F[String] =
    Trace[F].span("lookup greeting in DB") {
      // simulate reading the value from a DB
      Async[F].sleep(100.millis) *>
        Trace[F].put("name" -> TraceValue.StringValue(name)) *>
        s"Hello, $name!".pure[F]
    }

  def writeGreetingToCache(name: String, greeting: String): F[Unit] =
    Trace[F].span("write greeting to cache") {
      // simulate writing the value to a cache
      Trace[F].put(
        "name"     -> TraceValue.StringValue(name),
        "greeting" -> TraceValue.StringValue(greeting)
      ) *>
        Async[F].sleep(5.millis)
    }

  def lookupGreetingInDBAndWriteToCache(name: String): F[String] =
    for {
      greeting <- lookupGreetingInDB(name)
      _        <- writeGreetingToCache(name, greeting)
    } yield greeting
}
