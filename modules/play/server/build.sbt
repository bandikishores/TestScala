import Dependencies._

libraryDependencies ++= playDependencies ++ Seq(
  guice,
  ws,
  ehcache,
  //filters,
  "org.julienrf" %% "play-json-derived-codecs" % "6.0.0",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "com.typesafe.akka" %% "akka-protobuf" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Test,
  "org.awaitility" % "awaitility" % "4.0.1" % Test
)

PlayKeys.devSettings := Seq("play.server.http.port" -> "9001")

Test / parallelExecution := false

dockerBaseImage := "alpine:3.15"
dockerUpdateLatest := true
dockerExposedPorts ++= Seq(9001)
dockerExposedVolumes ++= Seq("/app/logs", "/app/common", "/app/session")
dockerEntrypoint ++= Seq("/opt/docker/bin/play-server")
dockerUsername := Option("1001")
//dockerAutoremoveMultiStageIntermediateImages := true

javaOptions in run ++= Seq(
  // JVM memory tuning
//  "-J-Xmx2048m",
//  "-J-Xms512m",
  "-J-Xss20m",
  // Since play uses separate pidfile we have to provide it with a proper path
  // name of the pid file must be play.pid
  //   s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",
  "-J-XX:+HeapDumpOnOutOfMemoryError",
  "-J-XX:HeapDumpPath=/app/logs/",
  // alternative, you can remove the PID file
  s"-Dpidfile.path=/dev/null",
  // Use separate configuration file for production environment
  s"-Dconfig.file=/opt/docker/conf/application.conf",
  // Use separate logger configuration file for production environment
  // s"-Dlogger.file=/usr/share/${packageName.value}/conf/production-logger.xml",

  // You may also want to include this setting if you use play evolutions
  "-DapplyEvolutions.default=true",
  "-Dlogger.file=/etc/logback/logback.xml",
  "-Dlogback.debug=true",
  "-Dhttp.port=9001"
)
