////////////////////
//// ROUTEGUIDE ////
////////////////////

lazy val `routeguide-protocol` = project
  .in(file("routeguide/protocol"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-monix"), mu("mu-rpc-fs2")))
  .settings(exampleRouteguideProtocolSettings)

lazy val `routeguide-runtime` = project
  .in(file("routeguide/runtime"))
  .settings(exampleRouteguideRuntimeSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `routeguide-common` = project
  .in(file("routeguide/common"))
  .dependsOn(`routeguide-protocol`)
  .settings(libraryDependencies ++= Seq(mu("mu-config")))
  .settings(exampleRouteguideCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `routeguide-server` = project
  .in(file("routeguide/server"))
  .dependsOn(`routeguide-common`)
  .dependsOn(`routeguide-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server")))
  .disablePlugins(SrcGenPlugin)

lazy val `routeguide-client` = project
  .in(file("routeguide/client"))
  .dependsOn(`routeguide-common`)
  .dependsOn(`routeguide-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-client-netty")))
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / "src" / "main" / "scala-io",
      baseDirectory.value / "src" / "main" / "scala-task"
    )
  )
  .settings(
    addCommandAlias("runClientIO", "runMain example.routeguide.client.io.ClientAppIO"),
    addCommandAlias("runClientTask", "runMain example.routeguide.client.task.ClientAppTask")
  )
  .disablePlugins(SrcGenPlugin)

lazy val routeguide = project
  .aggregate(
    `routeguide-protocol`,
    `routeguide-runtime`,
    `routeguide-common`,
    `routeguide-server`,
    `routeguide-client`
  )

////////////////////
/////   SEED   /////
////////////////////

lazy val `seed-config` = project
  .in(file("seed/config"))
  .settings(exampleSeedConfigSettings)

lazy val `seed-avro-protocol` = project
  .in(file("seed/protocol/avro"))
  .settings(exampleSeedAvroProtocolSettings)

lazy val `seed-protobuf-protocol` = project
  .in(file("seed/protocol/proto"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-fs2")))
  .settings(exampleSeedProtobufProtocolSettings)

lazy val `seed-server` = project
  .in(file("seed/server"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server")))
  .dependsOn(`seed-avro-protocol`, `seed-protobuf-protocol`, `seed-config`)
  .settings(exampleSeedLogSettings)

addCommandAlias("runAvroServer", "seed-server/runMain example.seed.server.app.AvroServerApp")
addCommandAlias("runProtoServer", "seed-server/runMain example.seed.server.app.ProtoServerApp")

lazy val `seed-client` = project
  .in(file("seed/client"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-client-netty"), mu("mu-rpc-fs2")))
  .dependsOn(`seed-avro-protocol`, `seed-protobuf-protocol`, `seed-config`)
  .settings(exampleSeedClientAppSettings)
  .settings(exampleSeedLogSettings)
  .disablePlugins(SrcGenPlugin)

addCommandAlias("runAvroClient", "seed-client/runMain example.seed.client.app.AvroClientApp")
addCommandAlias("runProtoClient", "seed-client/runMain example.seed.client.app.ProtoClientApp")

lazy val seed = project
  .aggregate(
    `seed-config`,
    `seed-avro-protocol`,
    `seed-protobuf-protocol`,
    `seed-client`,
    `seed-server`
  ) 
  .disablePlugins(SrcGenPlugin)

////////////////////
////  TODOLIST  ////
////////////////////

lazy val `todolist-protocol` = project
  .in(file("todolist/protocol"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-service")))
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-runtime` = project
  .in(file("todolist/runtime"))
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-client` = project
  .in(file("todolist/client"))
  .dependsOn(`todolist-protocol`)
  .dependsOn(`todolist-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-client-netty"), mu("mu-config")))
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-model` = project
  .in(file("todolist/model"))
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-persistence` = project
  .in(file("todolist/persistence"))
  .dependsOn(`todolist-model`)
  .dependsOn(`todolist-runtime`)
  .dependsOn(`todolist-protocol`)
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-server` = project
  .in(file("todolist/server"))
  .dependsOn(`todolist-protocol`)
  .dependsOn(`todolist-runtime`)
  .dependsOn(`todolist-model`)
  .dependsOn(`todolist-persistence`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server"), mu("mu-config")))
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val todolist = project
  .aggregate(
    `todolist-protocol`,
    `todolist-runtime`,
    `todolist-server`,
    `todolist-client`,
    `todolist-model`,
    `todolist-persistence`
  )

////////////////////////
////  HEALTH-CHECK  ////
////////////////////////

/////////HealthCheck Server Monix Example
lazy val `health-server-monix` = project
  .in(file("health-check/health-server-monix"))
  .settings(
    libraryDependencies ++= Seq(
      mu("mu-rpc-server"),
      mu("mu-rpc-monix"),
      mu("mu-rpc-health-check")
    )
  )
  .settings(healthCheckSettingsMonix)
  .disablePlugins(SrcGenPlugin)

/////////HealthCheck Server FS2 Example
lazy val `health-server-fs2` = project
  .in(file("health-check/health-server-fs2"))
  .settings(
    libraryDependencies ++= Seq(
      mu("mu-rpc-server"),
      mu("mu-rpc-fs2"),
      mu("mu-rpc-health-check")
    )
  )
  .settings(healthCheckSettingsFS2)
  .disablePlugins(SrcGenPlugin)

/////////HealthCheck Client Example
lazy val `health-client` = project
  .in(file("health-check/health-client"))
  .dependsOn(`health-server-monix`)
  .dependsOn(`health-server-fs2`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-client-netty"), mu("mu-config")))
  .settings(healthCheckSettingsMonix)
  .settings(healthCheckSettingsFS2)
  .disablePlugins(SrcGenPlugin)

lazy val `health-check` = project
  .aggregate(
    `health-server-monix`,
    `health-server-fs2`,
    `health-client`
  )

////////////////////
////  TRACING   ////
////////////////////

lazy val `tracing-protocol` = project
  .in(file("tracing/protocol"))
  .settings(tracingProtocolSettings)

lazy val `tracing-server-A` = project
  .in(file("tracing/serverA"))
  .dependsOn(`tracing-protocol`)
  .settings(tracingServerASettings)

lazy val `tracing-server-B` = project
  .in(file("tracing/serverB"))
  .dependsOn(`tracing-protocol`)
  .settings(tracingServerBSettings)

lazy val `tracing-client` = project
  .in(file("tracing/client"))
  .dependsOn(`tracing-protocol`)
  .settings(tracingClientSettings)

lazy val tracing = project
  .aggregate(
    `tracing-protocol`,
    `tracing-client`,
    `tracing-server-A`,
    `tracing-server-B`
  )

//////////////////////////
//// MODULES REGISTRY ////
//////////////////////////

lazy val allModules: Seq[ProjectReference] = Seq(
  `health-check`,
  routeguide,
  seed,
  todolist,
  tracing
)

lazy val root = project
  .in(file("."))
  .settings(name := "mu-scala-examples")
  .settings(noPublishSettings)
  .aggregate(allModules: _*)
  .disablePlugins(SrcGenPlugin)
