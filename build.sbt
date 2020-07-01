////////////////////
//// ROUTEGUIDE ////
////////////////////

lazy val `routeguide-protocol` = project
  .in(file("routeguide/protocol"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-monix")))
  .disablePlugins(SrcGenPlugin)

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

//// Shared Modules ////

lazy val `seed-config` = project
  .in(file("seed/shared/modules/config"))
  .settings(exampleSeedConfigSettings)
  .disablePlugins(SrcGenPlugin)

////     Shared     ////

lazy val allSharedModules: ProjectReference = `seed-config`

lazy val `seed-shared` = project
  .in(file("seed/shared"))
  .aggregate(allSharedModules)
  .disablePlugins(SrcGenPlugin)

//////////////////////////
////  Server Modules  ////
//////////////////////////

lazy val `seed-server-common` = project
  .in(file("seed/server/modules/common"))
  .disablePlugins(SrcGenPlugin)

lazy val `seed-server-protocol-avro` = project
  .in(file("seed/server/modules/protocol_avro"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-service")))
  .disablePlugins(SrcGenPlugin)

lazy val `seed-server-protocol-proto` = project
  .in(file("seed/server/modules/protocol_proto"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-fs2")))
  .disablePlugins(SrcGenPlugin)

lazy val `seed-server-process` = project
  .in(file("seed/server/modules/process"))
  .settings(exampleSeedLogSettings)
  .dependsOn(`seed-server-common`, `seed-server-protocol-avro`, `seed-server-protocol-proto`)
  .disablePlugins(SrcGenPlugin)

lazy val `seed-server-app` = project
  .in(file("seed/server/modules/app"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server")))
  .dependsOn(`seed-server-process`, `seed-config`)
  .disablePlugins(SrcGenPlugin)

//////////////////////////
////      Server      ////
//////////////////////////

lazy val allSeedServerModules: Seq[ProjectReference] = Seq(
  `seed-server-common`,
  `seed-server-protocol-avro`,
  `seed-server-protocol-proto`,
  `seed-server-process`,
  `seed-server-app`
)

lazy val `seed-server` = project
  .in(file("seed/server"))
  .aggregate(allSeedServerModules: _*)
  .disablePlugins(SrcGenPlugin)

addCommandAlias("runAvroServer", "seed-server/runMain example.seed.server.app.AvroServerApp")
addCommandAlias("runProtoServer", "seed-server/runMain example.seed.server.app.ProtoServerApp")

//////////////////////////
////  Client Modules  ////
//////////////////////////

lazy val `seed-client-common` = project
  .in(file("seed/client/modules/common"))
  .disablePlugins(SrcGenPlugin)

lazy val `seed-client-process` = project
  .in(file("seed/client/modules/process"))
  .settings(exampleSeedLogSettings)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-client-netty"), mu("mu-rpc-fs2")))
  .dependsOn(
    `seed-client-common`,
    `seed-server-protocol-avro`,
    `seed-server-protocol-proto`
  )
  .disablePlugins(SrcGenPlugin)

lazy val `seed-client-app` = project
  .in(file("seed/client/modules/app"))
  .settings(exampleSeedClientAppSettings)
  .dependsOn(`seed-client-process`, `seed-config`)
  .disablePlugins(SrcGenPlugin)

//////////////////////////
////      Client      ////
//////////////////////////

lazy val allSeedClientModules: Seq[ProjectReference] = Seq(
  `seed-client-common`,
  `seed-client-process`,
  `seed-client-app`
)

lazy val `seed-client` = project
  .in(file("seed/client"))
  .aggregate(allSeedClientModules: _*)
  .disablePlugins(SrcGenPlugin)

addCommandAlias("runAvroClient", "seed-client/runMain example.seed.client.app.AvroClientApp")
addCommandAlias("runProtoClient", "seed-client/runMain example.seed.client.app.ProtoClientApp")

// SEED root

lazy val allSeedModules: Seq[ProjectReference] = Seq(
  `seed-shared`,
  `seed-client`,
  `seed-server`
)

lazy val `seed` = project
  .in(file("seed"))
  .aggregate(allSeedModules: _*)
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

lazy val `todolist-model` = project
  .in(file("todolist/model"))
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-server` = project
  .in(file("todolist/server"))
  .dependsOn(`todolist-protocol`)
  .dependsOn(`todolist-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server"), mu("mu-config")))
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-client` = project
  .in(file("todolist/client"))
  .dependsOn(`todolist-protocol`)
  .dependsOn(`todolist-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-client-netty"), mu("mu-config")))
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)

lazy val `todolist-service` = project
  .in(file("todolist/service"))
  .dependsOn(`todolist-protocol`)
  .dependsOn(`todolist-runtime`)
  .dependsOn(`todolist-model`)
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)
 
lazy val `todolist-persistence` = project
  .in(file("todolist/persistence"))
  .dependsOn(`todolist-protocol`)
  .dependsOn(`todolist-runtime`)
  .dependsOn(`todolist-model`)
  .settings(exampleTodolistCommonSettings)
  .disablePlugins(SrcGenPlugin)  

lazy val todolist = project
  .aggregate(
    `todolist-protocol`,
    `todolist-runtime`,
    `todolist-server`,
    `todolist-client`
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
