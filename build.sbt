////////////////////
//// ROUTEGUIDE ////
////////////////////

lazy val `example-routeguide-protocol` = project
  .in(file("routeguide/protocol"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-monix")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-routeguide-protocol")

lazy val `example-routeguide-runtime` = project
  .in(file("routeguide/runtime"))
  .settings(noPublishSettings)
  .settings(coverageEnabled := false)
  .settings(moduleName := "mu-rpc-example-routeguide-runtime")
  .settings(exampleRouteguideRuntimeSettings)

lazy val `example-routeguide-common` = project
  .in(file("routeguide/common"))
  .dependsOn(`example-routeguide-protocol`)
  .settings(libraryDependencies ++= Seq(mu("mu-config")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-routeguide-common")
  .settings(exampleRouteguideCommonSettings)

lazy val `example-routeguide-server` = project
  .in(file("routeguide/server"))
  .dependsOn(`example-routeguide-common`)
  .dependsOn(`example-routeguide-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-routeguide-server")

lazy val `example-routeguide-client` = project
  .in(file("routeguide/client"))
  .dependsOn(`example-routeguide-common`)
  .dependsOn(`example-routeguide-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-netty")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-routeguide-client")
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / "src" / "main" / "scala-io",
      baseDirectory.value / "src" / "main" / "scala-task"
    )
  )
  .settings(addCommandAlias("runClientIO", "runMain example.routeguide.client.io.ClientAppIO"))
  .settings(
    addCommandAlias("runClientTask", "runMain example.routeguide.client.task.ClientAppTask")
  )

////////////////////
/////   SEED   /////
////////////////////

//// Shared Modules ////

lazy val `seed-config` = project
  .in(file("seed/shared/modules/config"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(exampleSeedConfigSettings)

////     Shared     ////

lazy val allSharedModules: ProjectReference = `seed-config`

lazy val allSharedModulesDeps: ClasspathDependency =
  ClasspathDependency(allSharedModules, None)

lazy val `seed-shared` = project
  .in(file("seed/shared"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .aggregate(allSharedModules)
  .dependsOn(allSharedModulesDeps)

//////////////////////////
////  Server Modules  ////
//////////////////////////

lazy val `seed-server-common` = project
  .in(file("seed/server/modules/common"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)

lazy val `seed-server-protocol-avro` = project
  .in(file("seed/server/modules/protocol_avro"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-channel")))

lazy val `seed-server-protocol-proto` = project
  .in(file("seed/server/modules/protocol_proto"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-fs2")))

lazy val `seed-server-process` = project
  .in(file("seed/server/modules/process"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(exampleSeedLogSettings)
  .dependsOn(`seed-server-common`, `seed-server-protocol-avro`, `seed-server-protocol-proto`)

lazy val `seed-server-app` = project
  .in(file("seed/server/modules/app"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server")))
  .dependsOn(`seed-server-process`, `seed-config`)

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

lazy val allSeedServerModulesDeps: Seq[ClasspathDependency] =
  allSeedServerModules.map(ClasspathDependency(_, None))

lazy val `seed-server` = project
  .in(file("seed/server"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .aggregate(allSeedServerModules: _*)
  .dependsOn(allSeedServerModulesDeps: _*)
addCommandAlias("runAvroServer", "seed_server/runMain example.seed.server.app.AvroServerApp")
addCommandAlias("runProtoServer", "seed_server/runMain example.seed.server.app.ProtoServerApp")

//////////////////////////
////  Client Modules  ////
//////////////////////////

lazy val `seed-client-common` = project
  .in(file("seed/client/modules/common"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)

lazy val `seed-client-process` = project
  .in(file("seed/client/modules/process"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(exampleSeedLogSettings)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-netty"), mu("mu-rpc-fs2")))
  .dependsOn(
    `seed-client-common`,
    `seed-server-protocol-avro`,
    `seed-server-protocol-proto`
  )

lazy val `seed-client-app` = project
  .in(file("seed/client/modules/app"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(exampleSeedClientAppSettings)
  .dependsOn(`seed-client-process`, `seed-config`)

//////////////////////////
////      Client      ////
//////////////////////////

lazy val allSeedClientModules: Seq[ProjectReference] = Seq(
  `seed-client-common`,
  `seed-client-process`,
  `seed-client-app`
)

lazy val allSeedClientModulesDeps: Seq[ClasspathDependency] =
  allSeedClientModules.map(ClasspathDependency(_, None))

lazy val `seed-client` = project
  .in(file("seed/client"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .aggregate(allSeedClientModules: _*)
  .dependsOn(allSeedClientModulesDeps: _*)
addCommandAlias("runAvroClient", "seed_client/runMain example.seed.client.app.AvroClientApp")
addCommandAlias("runProtoClient", "seed_client/runMain example.seed.client.app.ProtoClientApp")

// SEED root

lazy val allSeedModules: Seq[ProjectReference] = Seq(
  `seed-shared`,
  `seed-client`,
  `seed-server`
)

lazy val allSeedModulesDeps: Seq[ClasspathDependency] =
  allSeedModules.map(ClasspathDependency(_, None))

lazy val `example-seed` = project
  .in(file("seed"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-seed")
  .aggregate(allSeedModules: _*)
  .dependsOn(allSeedModulesDeps: _*)

////////////////////
////  TODOLIST  ////
////////////////////

lazy val `example-todolist-protocol` = project
  .in(file("todolist/protocol"))
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-channel")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-todolist-protocol")

lazy val `example-todolist-runtime` = project
  .in(file("todolist/runtime"))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-todolist-runtime")

lazy val `example-todolist-server` = project
  .in(file("todolist/server"))
  .dependsOn(`example-todolist-protocol`)
  .dependsOn(`example-todolist-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-server"), mu("mu-config")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-todolist-server")
  .settings(exampleTodolistCommonSettings)

lazy val `example-todolist-client` = project
  .in(file("todolist/client"))
  .dependsOn(`example-todolist-protocol`)
  .dependsOn(`example-todolist-runtime`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-netty"), mu("mu-config")))
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-todolist-client")
  .settings(exampleTodolistCommonSettings)

////////////////////////
////  HEALTH-CHECK  ////
////////////////////////

/////////HealthCheck Server Monix Example
lazy val `example-health-server-monix` = project
  .in(file("health-check/health-server-monix"))
  .settings(
    libraryDependencies ++= Seq(
      mu("mu-rpc-server"),
      mu("mu-rpc-monix"),
      mu("mu-rpc-health-check-unary")
    )
  )
  .settings(healthCheckSettingsMonix)
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-health-check-server-monix")

/////////HealthCheck Server FS2 Example
lazy val `example-health-server-fs2` = project
  .in(file("health-check/health-server-fs2"))
  .settings(
    libraryDependencies ++= Seq(
      mu("mu-rpc-server"),
      mu("mu-rpc-fs2"),
      mu("mu-rpc-health-check-unary")
    )
  )
  .settings(healthCheckSettingsFS2)
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-health-check-server-fs2")

/////////HealthCheck Client Example
lazy val `example-health-client` = project
  .in(file("health-check/health-client"))
  .dependsOn(`example-health-server-monix`)
  .dependsOn(`example-health-server-fs2`)
  .settings(libraryDependencies ++= Seq(mu("mu-rpc-netty"), mu("mu-config")))
  .settings(healthCheckSettingsMonix)
  .settings(healthCheckSettingsFS2)
  .settings(coverageEnabled := false)
  .settings(noPublishSettings)
  .settings(moduleName := "mu-rpc-example-health-check-client")

//////////////////////////
//// MODULES REGISTRY ////
//////////////////////////

lazy val allModules: Seq[ProjectReference] = Seq(
  `example-health-client`,
  `example-health-server-monix`,
  `example-health-server-fs2`,
  `example-routeguide-protocol`,
  `example-routeguide-common`,
  `example-routeguide-runtime`,
  `example-routeguide-server`,
  `example-routeguide-client`,
  `example-seed`,
  `example-todolist-protocol`,
  `example-todolist-runtime`,
  `example-todolist-server`,
  `example-todolist-client`
)

lazy val allModulesDeps: Seq[ClasspathDependency] =
  allModules.map(ClasspathDependency(_, None))

lazy val root = project
  .in(file("."))
  .settings(name := "mu-scala-examples")
  .settings(noPublishSettings)
  .aggregate(allModules: _*)
  .dependsOn(allModulesDeps: _*)
