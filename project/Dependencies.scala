import sbt._
import sbt.internal.util.ManagedLogger

import scala.sys.process.ProcessLogger

object Dependencies {

  val scalaVer = "2.12.11"

  val akkaHttpVersion = "10.1.11"

  val akkaVersion = "2.6.5"
  val awaitilityVersion = "4.0.2"
  val jimfsVersion = "1.1"
  val awsVersion = "1.11.774"
  val bootstrapVersion = "4.4.1"
  val circeVersion = "0.13.0"
  val commonCodecVersion = "1.14"
  val commonIoVersion = "2.6"
  val commonLang3Version = "3.10"
  val commonLangVersion = "2.6"
  val commonTextVersion = "1.8"
  val dockerClientVersion = "3.2.1"
  val drunkVersion = "2.5.0"
  val enumeratumVersion = "1.5.13"
  val ficusVersion = "1.4.7"
  val futureQueueVersion = "1.2.0"
  val guiceVersion = "4.2.3"
  val nulabOAuth2Version = "1.3.0"
  val hadoopVersion = "2.6.5"
  val web4jVersion = "3.3.1"
  val hiveJdbcVersion = "2.3.5"
  val jacksonVersion = "2.11.0"
  val jakartaVersion = "2.1.6"
  val javaFakerVersion = "1.0.2"
  val jgitVersion = "5.12.0.202106070339-r"
  val jodaVersion = "2.10.6"
  val jqueryVersion = "3.5.0"
  val jsonAvroVersion = "0.2.9"
  val jtsVersion = "1.13"
  val liftVersion = "3.4.1"
  val livyVersion = "0.7.1-incubating" // on change, change version in init.sh of sparkedge
  val logbackVersion = "1.2.3"
  val mavenInvVersion = "3.0.1"
  val moultingYamlVersion = "0.4.2"
  val playBootstrapVersion = "1.2-P26-B3"
  val playJodaVersion = "2.6.0"
  val playJsonVersion = "2.6.13"
  val playMailerVersion = "6.0.0"
  val playSilhouetteVersion = "5.0.6"
  val playSlickVersion = "3.0.3"
  val playVersion = "2.6.25"
  val nettyVersion = "4.1.46.Final"
  val postgresVersion = "9.4.1212"
  val pprintVersion = "0.5.9"
  val requireJsVersion = "2.3.6"
  val sangriaCirceVersion = "1.3.0"
  val sangriaPlayVersion = "2.0.1"
  val sangriaVersion = "1.4.2"
  val scalaCsvVersion = "1.3.6"
  val scalaGuiceVersion = "4.2.6"
  val scalaParserComVersion = "1.1.1"
  val scalaTestPlayVersion = "3.1.2"
  val scalaTestVersion = "3.0.1"
  val scalaLoggingVersion = "3.9.2"
  val scalaMockVersion = "4.4.0"
  val scalaXmlVersion = "1.1.1"
  val scalaformatterVersion = "2.5.0"
  val calciteVersion = "1.25.0"
  val jsqlParserVersion = "3.2"
  val scalajVersion = "2.4.2"
  val scalametaVersion = "4.3.24"
  val scalazVersion = "7.3.0"
  val scallopVersion = "3.4.0"
  val shapelessVersion = "2.3.3"
  val slf4jVersion = "1.7.30"
  val slickPgVersion = "0.17.1"
  val sparkVersion = "3.0.1"
  val sparkTestingBaseVersion = "3.0.1_1.0.0"
  val stripeVersion = "19.8.0"
  val sttpVersion = "1.5.0"
  val webjarsVersion = "2.6.3"
  val kamonVersion = "2.3.1"
  val shiroVersion = "1.4.2"
  val pdiJwtCoreVersion = "4.2.0"
  val catsVersion = "2.0.0"
  val playJsonDerivedCodecsVersion = "6.0.0"
  val elastic4sVersion = "7.10.0"

  val excludeCirceBinding = ExclusionRule(organization = "io.circe")

  val jwtDependencies = Seq(
    "com.pauldijou" %% "jwt-core",
    "com.pauldijou" %% "jwt-play-json",
    "com.pauldijou" %% "jwt-circe"
  ).map(_ % pdiJwtCoreVersion)

  val commonDependencies = Seq(
    "org.scala-lang" % "scala-library" % scalaVer,
    "org.scala-lang" % "scala-compiler" % scalaVer,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "io.kamon" %% "kamon-bundle" % kamonVersion,
    "io.kamon" %% "kamon-prometheus" % kamonVersion,
    "io.kamon" %% "kamon-jdbc" % kamonVersion,
    "io.kamon" %% "kamon-jaeger" % kamonVersion,
    ("commons-lang" % "commons-lang" % commonLangVersion).withSources(),
    ("org.apache.commons" % "commons-lang3" % commonLang3Version).withSources(),
    ("commons-io" % "commons-io" % commonIoVersion).withSources(),
    "net.codingwell" %% "scala-guice" % scalaGuiceVersion,
    "org.rogach" %% "scallop" % scallopVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.scalamock" %% "scalamock" % scalaMockVersion % Test,
    "org.awaitility" % "awaitility" % awaitilityVersion % Test,
    "com.google.jimfs" % "jimfs" % jimfsVersion % Test,
    "com.thoughtworks.extractor" %% "extractor" % "latest.release",
    "org.typelevel" %% "cats-core" % catsVersion
  )

  val loggingDependencies = Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "org.slf4j" % "log4j-over-slf4j" % slf4jVersion
  )

  val playDependencies = Seq(
    "com.typesafe.play" %% "play-akka-http-server" % playVersion,
    "com.typesafe.play" %% "play-guice" % playVersion,
    "com.typesafe.play" %% "play-json" % playJsonVersion,
    "com.typesafe.play" %% "filters-helpers" % playVersion,
    "com.typesafe.play" %% "play-netty-server" % playVersion,
    "com.typesafe.play" %% "play-logback" % playVersion,
    "io.netty" % "netty-all" % nettyVersion,
    "io.netty" % "netty-buffer" % nettyVersion,
    "io.netty" % "netty-codec" % nettyVersion,
    "io.netty" % "netty-codec-http" % nettyVersion,
    "io.netty" % "netty-common" % nettyVersion,
    "io.netty" % "netty-handler" % nettyVersion,
    "io.netty" % "netty-resolver" % nettyVersion,
    "io.netty" % "netty-transport" % nettyVersion,
    "com.typesafe.play" %% "play-logback" % playVersion,
    "com.typesafe.play" %% "play-ehcache" % playVersion,
    "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.12",
    "com.typesafe.play" %% "play-specs2" % playVersion % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlayVersion % Test,
    "com.h2database" % "h2" % "1.4.192" % Test
  )

  val silhouetteDependencies = Seq(
    "com.mohiva" %% "play-silhouette" % playSilhouetteVersion,
    "com.mohiva" %% "play-silhouette-password-bcrypt" % playSilhouetteVersion,
    "com.mohiva" %% "play-silhouette-persistence" % playSilhouetteVersion,
    "com.mohiva" %% "play-silhouette-crypto-jca" % playSilhouetteVersion,
    "com.mohiva" %% "play-silhouette-testkit" % playSilhouetteVersion
  )

  val webjarDependencies = Seq(
    "org.webjars" % "requirejs" % requireJsVersion,
    ("org.webjars" % "bootstrap" % bootstrapVersion).exclude("org.webjars", "jquery"),
    "org.webjars" % "jquery" % jqueryVersion,
    "org.webjars" %% "webjars-play" % webjarsVersion,
    "com.adrianhurt" %% "play-bootstrap" % playBootstrapVersion
  )

  val sttpDependencies = Seq(
    "com.softwaremill.sttp" %% "core",
    "com.softwaremill.sttp" %% "akka-http-backend",
    "com.softwaremill.sttp" %% "play-json",
    "com.softwaremill.sttp" %% "circe"
  ).map(_ % sttpVersion)
    .map(x ⇒ x.excludeAll(excludeCirceBinding))

  val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion
  )

  val circeDependencies = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-yaml",
    "io.circe" %% "circe-generic-extras"
  ).map(_ % circeVersion)

  val enumeratumDependencies =
    Seq(
      "com.beachape" %% "enumeratum",
      "com.beachape" %% "enumeratum-play-json",
      "com.beachape" %% "enumeratum-play",
      "com.beachape" %% "enumeratum-circe"
    )
      .map(_ % enumeratumVersion)
      .map(x ⇒ x.excludeAll(excludeCirceBinding))

  val sparkDependencies = Seq(
    ("org.apache.spark" %% "spark-sql" % sparkVersion)
      .exclude("org.slf4j", "slf4j-log4j12")
      .exclude("org.slf4j", "*")
      .excludeAll(ExclusionRule(organization = "org.slf4j"))
      .exclude("javax.ws.rs", "javax.ws.rs-api")
      .exclude("io.netty", "netty-all"),
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.apache.spark" %% "spark-avro" % sparkVersion,
    "jakarta.ws.rs" % "jakarta.ws.rs-api" % jakartaVersion,
    "com.holdenkarau" %% "spark-testing-base" % sparkTestingBaseVersion % Test
  )
  def debugOnPort(port: Int) = s"-J-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$port"

  implicit def processLoggerToManagedLogger(log: ManagedLogger): ProcessLogger = new ProcessLogger {
    override def out(s: ⇒ String): Unit = log.info(s)
    // for some reason, normal logs are going to stderr. So putting them to log.info instead of log.error
    override def err(s: ⇒ String): Unit = log.info(s)

    override def buffer[T](f: ⇒ T): T = f
  }

}
