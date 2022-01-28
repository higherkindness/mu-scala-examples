import sbt.Keys._
import sbt._
import higherkindness.mu.rpc.srcgen.Model._
import higherkindness.mu.rpc.srcgen.SrcGenPlugin.autoImport._

import scala.language.reflectiveCalls

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val V = new {
      val catsEffect: String    = "3.3.4"
      val circe: String         = "0.14.1"
      val doobie: String        = "1.0.0-RC2"
      val fs2: String           = "3.2.4"
      val kindProjector: String = "0.13.2"
      val log4cats: String      = "2.2.0"
      val log4s: String         = "1.10.0"
      val logback: String       = "1.2.10"
      val mu: String            = "0.27.4"
      val natchez: String       = "0.1.6"
      val pureconfig: String    = "0.17.1"
      val scala213: String      = "2.13.8"
      val scopt: String         = "4.0.1"
    }

    def mu(module: String) = "io.higherkindness" %% module % V.mu

    lazy val macroSettings: Seq[Setting[_]] = {

      Seq(
        libraryDependencies ++= Seq(
          scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided
        ),
        scalacOptions ++= Seq("-Ymacro-annotations")
      )
    }

    lazy val healthCheckSettingsFS2: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "log4cats-core"  % V.log4cats,
        "org.typelevel" %% "log4cats-slf4j" % V.log4cats,
        "co.fs2"        %% "fs2-core"       % V.fs2,
        "org.typelevel" %% "cats-effect"    % V.catsEffect
      )
    )

    lazy val exampleRouteguideRuntimeSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "co.fs2" %% "fs2-core" % V.fs2
      )
    )

    lazy val exampleRouteguideProtocolSettings: Seq[Def.Setting[_]] = Seq(
      muSrcGenIdlType            := IdlType.Proto,
      muSrcGenIdiomaticEndpoints := true
    )

    lazy val exampleRouteguideCommonSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.circe"      %% "circe-core"      % V.circe,
        "io.circe"      %% "circe-generic"   % V.circe,
        "io.circe"      %% "circe-parser"    % V.circe,
        "org.log4s"     %% "log4s"           % V.log4s,
        "ch.qos.logback" % "logback-classic" % V.logback
      )
    )

    lazy val exampleSeedLogSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % V.logback,
        "org.typelevel" %% "log4cats-core"   % V.log4cats,
        "org.typelevel" %% "log4cats-slf4j"  % V.log4cats
      )
    )

    lazy val exampleSeedConfigSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "org.typelevel"         %% "cats-effect" % V.catsEffect,
        "com.github.pureconfig" %% "pureconfig"  % V.pureconfig
      )
    )

    lazy val exampleSeedProtobufProtocolSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-fs2"),
        mu("mu-rpc-service")
      ),
      muSrcGenIdlType            := IdlType.Proto,
      muSrcGenIdiomaticEndpoints := true,
      scalacOptions += "-Wconf:src=src_managed/.*:silent"
    )

    lazy val exampleSeedAvroProtocolSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-service")
      ),
      muSrcGenIdlType            := IdlType.Avro,
      muSrcGenIdiomaticEndpoints := true,
      scalacOptions += "-Wconf:src=src_managed/.*:silent"
    )

    lazy val exampleSeedClientAppSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % V.scopt
      )
    )

    lazy val exampleTodolistCommonSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "org.tpolecat"  %% "doobie-core"     % V.doobie,
        "org.tpolecat"  %% "doobie-h2"       % V.doobie,
        "org.tpolecat"  %% "doobie-hikari"   % V.doobie,
        "org.typelevel" %% "log4cats-core"   % V.log4cats,
        "org.typelevel" %% "log4cats-slf4j"  % V.log4cats,
        "ch.qos.logback" % "logback-classic" % V.logback
      )
    )

    lazy val tracingProtocolSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-service"),
        mu("mu-rpc-fs2")
      ),
      muSrcGenIdlType            := IdlType.Proto,
      muSrcGenIdiomaticEndpoints := true
    )

    lazy val tracingServerASettings: Seq[Def.Setting[_]] = Seq(
      fork := true,
      libraryDependencies ++= Seq(
        mu("mu-rpc-server"),
        mu("mu-rpc-client-netty"),
        "org.tpolecat" %% "natchez-jaeger" % V.natchez,
        "org.slf4j"     % "slf4j-simple"   % "1.7.33"
      ).map(_.exclude("org.slf4j", "slf4j-jdk14"))
    )

    lazy val tracingServerBSettings: Seq[Def.Setting[_]] = Seq(
      fork := true,
      libraryDependencies ++= Seq(
        mu("mu-rpc-server"),
        "org.tpolecat" %% "natchez-jaeger" % V.natchez,
        "org.slf4j"     % "slf4j-simple"   % "1.7.33"
      ).map(_.exclude("org.slf4j", "slf4j-jdk14"))
    )

    lazy val tracingClientSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-client-netty"),
        "org.tpolecat" %% "natchez-jaeger" % V.natchez,
        "org.slf4j"     % "slf4j-simple"   % "1.7.33"
      )
    )

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      description := "mu-scala-examples https://github.com/higherkindness/mu-scala",
      scalacOptions --= Seq("-Xfuture", "-Xfatal-warnings"),
      scalacOptions ++= Seq(
        "-Xlint:-missing-interpolator",
        "-Xlint:-byname-implicit"
      ), // per https://github.com/scala/bug/issues/12072, we need to disable the warnings from doobie
      addCompilerPlugin(
        "org.typelevel" %% "kind-projector" % V.kindProjector cross CrossVersion.full
      )
    ) ++ macroSettings
}
