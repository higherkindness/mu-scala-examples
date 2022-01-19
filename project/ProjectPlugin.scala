import sbt.Keys._
import sbt._
import sbtorgpolicies.OrgPoliciesPlugin
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.model._
import sbtorgpolicies.templates._
import sbtorgpolicies.templates.badges._
import sbtorgpolicies.runnable.syntax._
import higherkindness.mu.rpc.srcgen.Model._
import higherkindness.mu.rpc.srcgen.SrcGenPlugin.autoImport._

import scala.language.reflectiveCalls

object ProjectPlugin extends AutoPlugin {

  override def requires: Plugins = OrgPoliciesPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val V = new {
      val catsEffect: String    = "2.1.3"
      val circe: String         = "0.13.0"
      val doobie: String        = "0.9.0"
      val fs2: String           = "2.3.0"
      val kindProjector: String = "0.11.0"
      val log4cats: String      = "1.0.1"
      val log4s: String         = "1.8.2"
      val logback: String       = "1.2.3"
      val monix: String         = "3.2.1"
      val mu: String            = "0.22.3"
      val natchez: String       = "0.0.11"
      val paradise: String      = "2.1.1"
      val pureconfig: String    = "0.12.3"
      val scala213: String      = "2.13.3"
      val scopt: String         = "3.7.1"
      val slf4j: String         = "1.7.30"
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
        "io.chrisdavenport" %% "log4cats-core"  % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
        "org.slf4j"          % "slf4j-simple"   % V.slf4j,
        "co.fs2"            %% "fs2-core"       % V.fs2,
        "org.typelevel"     %% "cats-effect"    % V.catsEffect
      )
    )

    lazy val healthCheckSettingsMonix: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.chrisdavenport" %% "log4cats-core"  % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
        "org.slf4j"          % "slf4j-simple"   % V.slf4j,
        "io.monix"          %% "monix"          % V.monix,
        "org.typelevel"     %% "cats-effect"    % V.catsEffect
      )
    )

    lazy val exampleRouteguideRuntimeSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.monix" %% "monix" % V.monix
      )
    )

    lazy val exampleRouteguideProtocolSettings: Seq[Def.Setting[_]] = Seq(
      muSrcGenIdlType := IdlType.Proto,
      muSrcGenStreamingImplementation := higherkindness.mu.rpc.srcgen.Model.MonixObservable,
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
        "ch.qos.logback"     % "logback-classic" % V.logback,
        "io.chrisdavenport" %% "log4cats-core"   % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j"  % V.log4cats
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
      muSrcGenIdlType := IdlType.Proto,
      muSrcGenIdiomaticEndpoints := true
    )

    lazy val exampleSeedAvroProtocolSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-service")
      ),
      muSrcGenIdlType := IdlType.Avro,
      muSrcGenIdiomaticEndpoints := true
    )

    lazy val exampleSeedClientAppSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % V.scopt
      )
    )

    lazy val exampleTodolistCommonSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "org.tpolecat"      %% "doobie-core"     % V.doobie,
        "org.tpolecat"      %% "doobie-h2"       % V.doobie,
        "org.tpolecat"      %% "doobie-hikari"   % V.doobie,
        "io.chrisdavenport" %% "log4cats-core"   % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j"  % V.log4cats,
        "ch.qos.logback"     % "logback-classic" % V.logback
      )
    )

    lazy val tracingProtocolSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-service"),
        mu("mu-rpc-fs2")
      ),
      muSrcGenIdlType := IdlType.Proto,
      muSrcGenIdiomaticEndpoints := true
    )

    lazy val tracingServerASettings: Seq[Def.Setting[_]] = Seq(
      fork := true,
      libraryDependencies ++= Seq(
        mu("mu-rpc-server"),
        mu("mu-rpc-client-netty"),
        "org.tpolecat" %% "natchez-jaeger" % V.natchez,
        "org.slf4j"     % "slf4j-simple"   % "1.7.30"
      ).map(_.exclude("org.slf4j", "slf4j-jdk14"))
    )

    lazy val tracingServerBSettings: Seq[Def.Setting[_]] = Seq(
      fork := true,
      libraryDependencies ++= Seq(
        mu("mu-rpc-server"),
        "org.tpolecat" %% "natchez-jaeger" % V.natchez,
        "org.slf4j"     % "slf4j-simple"   % "1.7.30"
      ).map(_.exclude("org.slf4j", "slf4j-jdk14"))
    )

    lazy val tracingClientSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        mu("mu-rpc-client-netty"),
        "dev.profunktor" %% "console4cats"   % "0.8.1",
        "org.tpolecat"   %% "natchez-jaeger" % V.natchez,
        "org.slf4j"       % "slf4j-simple"   % "1.7.30"
      )
    )

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      description := "mu-scala-examples https://github.com/higherkindness/mu-scala",
      startYear := Some(2020),
      orgProjectName := "mu-scala-examples",
      orgGithubSetting := GitHubSettings(
        organization = "higherkindness",
        project = (name in LocalRootProject).value,
        organizationName = "47 Degrees",
        groupId = "io.higherkindness",
        organizationHomePage = url("http://47deg.com"),
        organizationEmail = "hello@47deg.com"
      ),
      scalaVersion := V.scala213,
      scalacOptions --= Seq("-Xfuture", "-Xfatal-warnings"),
      scalacOptions ++= Seq(
        "-Xlint:-missing-interpolator",
        "-Xlint:-byname-implicit"
      ), // per https://github.com/scala/bug/issues/12072, we need to disable the warnings from doobie
      addCompilerPlugin(
        "org.typelevel" %% "kind-projector" % V.kindProjector cross CrossVersion.full
      ),
      orgMaintainersSetting := List(
        Dev("developer47deg", Some("47 Degrees (twitter: @47deg)"), Some("hello@47deg.com"))
      )
    ) ++ macroSettings
}
