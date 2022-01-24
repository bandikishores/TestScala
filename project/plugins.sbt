//addSbtCoursier
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.6")
addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.35")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")
//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")

//addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.16")
//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
//resolvers += "bandi2".at("https://bandi2.jfrog.io/artifactory/sbt-repo/")
addSbtPlugin("io.kamon" % "sbt-kanela-runner-play-2.6" % "2.0.12")

//addSbtPlugin("io.kamon"         % "sbt-aspectj-runner-play-2.6" % "1.1.2")
addSbtPlugin("com.github.mwz" % "sbt-sonar" % "2.2.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
// Release plugin https://github.com/sbt/sbt-release
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
// To run LOC calculation: (1) Enable below plugin and (2) run `stats` sbt command
// For more information check out: https://github.com/orrsella/sbt-stats
//addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.7")
//addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.26")
// Caliban Plugin for a generating graphql client (used by metadata)
// Using latest snapshot until the release with the PR: https://github.com/ghostdogpr/caliban/pull/1140
resolvers += "plugins-sonatype-snapshots".at("https://oss.sonatype.org/content/repositories/snapshots/")
addSbtPlugin("com.github.ghostdogpr" % "caliban-codegen-sbt" % "1.2.3+6-8511501f-SNAPSHOT")

// Scala Benchmarking
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.3")
