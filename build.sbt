import Dependencies._
import Dependencies213._
import sbt._
import Keys._

import scala.sys.process._
import com.typesafe.sbt.packager.docker.DockerAlias

name := "sample-scala-project"
organization := "com.bandi"
version := "1.0"

libraryDependencies ++= commonDependencies ++ loggingDependencies ++ playDependencies ++ Seq(
  "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion,
  "org.scalaj" %% "scalaj-http" % scalajVersion,
)

lazy val playServer = (project in module("play/server"))
  .settings(commonSettings)
  .settings(dockerPluginSettings)
  .settings(
    name := """play-server""",
  )
  .enablePlugins(PlayScala, JavaAgent, JavaAppPackaging)
  .dependsOn(
    // commonCore % "compile->compile;test->test",
  )

lazy val root = (project in file("."))
  .aggregate(
    playServer,
  )
  .settings(doNotPublish)
  .settings(
    publishDockerImages := {
      // keeping it for now. Will remove later.
      Process("./x/publish-docker-images.sh", baseDirectory.value) ! streams.value.log
    },
    releaseProcess := Seq[ReleaseStep]( // https://github.com/sbt/sbt-release
      runDockerPublish, // docker:publish
    ),
    // copied from
    // https://github.com/opt-tech/sbt-diff-project/blob/master/src/main/scala/jp/ne/opt/sbt/diff/SbtPlugin.scala#L41
    commands += Command.args("evaluate-diffs", "<arg>") { (state, args) ⇒
      val files = s"git diff --name-only ${args.mkString(" ")}".lineStream_!.toList.collect {
        case line if line.trim.nonEmpty ⇒ new File(line)
      }
      val buildRoot = new File(loadedBuild.value.root.getPath)

      val projectMap = loadedBuild.value.allProjectRefs.toMap

      val reverseDependencyMap = buildDependencies.value.classpathTransitive
        .foldLeft[Map[sbt.ProjectRef, Seq[sbt.ResolvedProject]]](Map.empty) { (acc, dependency) ⇒
          val (ref, dependOns) = dependency

          dependOns.foldLeft(acc) { (dependencyMap, key) ⇒
            val resolvedProjects = dependencyMap.getOrElse(key, Nil)
            val newValue = projectMap.get(ref).fold(resolvedProjects)(_ +: resolvedProjects)
            dependencyMap + (key → newValue)
          }
        }

      val (modifiedState, diffProjects, diffProjectsWithDependents) =
        loadedBuild.value.allProjectRefs.foldLeft(state, List.empty[ResolvedProject], List.empty[ResolvedProject]) {
          case ((currentState, projects, projectsWithDependents), (ref, project)) ⇒
            if (files.exists(_.absolutePath.contains(project.base.absolutePath))) {
              (
                currentState,
                project :: projects,
                project :: (projectsWithDependents ++ reverseDependencyMap.get(ref).toList.flatten)
              )
            } else {
              (currentState, projects, projectsWithDependents)
            }
        }
      /*
       * remove duplicates and filter root project
       */
      val changedProjects: Set[String] = diffProjects
        .groupBy(_.id)
        .flatMap { case (_, xs) ⇒ xs.headOption }
        .filter { project ⇒
          project.base.absolutePath != buildRoot.absolutePath
        }
        .map(_.id)
        .toSet
      val changedProjectsWithDependents: Set[String] = diffProjectsWithDependents
        .groupBy(_.id)
        .flatMap { case (_, xs) ⇒ xs.headOption }
        .filter { project ⇒
          project.base.absolutePath != buildRoot.absolutePath
        }
        .map(_.id)
        .toSet
      println(s"export CHANGED_PROJECTS=${changedProjects.mkString(",")}")
      println(s"export PROJECTS_TO_BUILD=${changedProjectsWithDependents.mkString(",")}")
      modifiedState
    },
    commands += Command.args("set-next-snapshot-version", "major/minor/patch") { (state, args) ⇒
      state
      // This is WIP. come back to this later.
//      val bump = args.head.toLowerCase match {
//        case "major" ⇒ Version.Bump.Major
//        case "minor" ⇒ Version.Bump.Minor
//        case "bugfix" ⇒ Version.Bump.Bugfix
//      }
//      val nextVersion = bump.bump(Version(version.value).get)
//      val versionStr = s"""version in ThisBuild := "$nextVersion"""".stripMargin
//      IO.writeLines(baseDirectory.value / "version.sbt", Seq(versionStr))
//      reapply(Seq(version := nextVersion.toString), state)
    }
  )

dockerExposedVolumes ++= Seq("/app/logs", "/app/common", "/app/session")
// dockerAutoremoveMultiStageIntermediateImages := true


logLevel := Level.Error

lazy val dockerTagsToPush = settingKey[List[String]]("tags to push")
Global / dockerTagsToPush := {
  val vcs = releaseVcs.value.get
  val branch = vcs.currentBranch
  val tags =
    if (branch.endsWith("master"))
      "latest" :: "master" :: version.value :: Nil
    else if (branch.endsWith("release"))
      "release" :: version.value :: Nil
    else
      branch.replaceAll("/+", "_") :: Nil
  println(s"Branch: $branch. Docker tags to push: $tags")
  tags
}

lazy val dockerTagsToBuild = settingKey[List[String]]("tags to build")
Global / dockerTagsToBuild := {
  val vcs = releaseVcs.value.get
  val branch = vcs.currentBranch
  val tags0 =
    if (branch.endsWith("master"))
      "latest" :: "master" :: version.value :: Nil
    else if (branch.endsWith("release"))
      "latest" :: "release" :: version.value :: Nil
    else
      "latest" :: version.value :: branch.replaceAll("/+", "_") :: Nil
  // If building PR, then also tag images as the base branch
  val tags = sys.env.get("PR_BASE_BRANCH") match {
    case Some(value) ⇒ value.replaceAll("/+", "_") :: tags0
    case None ⇒ tags0
  }
  println(s"Branch: $branch. Docker tags to build: $tags")
  tags
}
lazy val dockerRepo = Some("gcr.io/bandi-deployments")

// for non-docker projects. Sometimes sbt is stupid
lazy val nonDockerSettings: Seq[Setting[_]] = Seq(
  Docker / publishLocal := {},
  Docker / publish := {}
)
// we don't publish any poms of our projects
lazy val doNotPublish: Seq[Setting[_]] = Seq(publish / skip := true)

lazy val dockerPluginSettings: Seq[Setting[_]] = Seq(
  Docker / dockerUpdateLatest := false,
  Docker / dockerBuildCommand := {
    val alias = (Docker / dockerAlias).value
    val dockerTagArgs: Seq[String] = for { // -t svc:latest -t svc:tag -t gcr/svc:latest -t gcr/svs:tag ...
      aliasWithoutTag ← Seq(alias, alias.withRegistryHost(None).withUsername(None))
      tagToBuild ← dockerTagsToBuild.value
      alias = aliasWithoutTag.withTag(Some(tagToBuild))
      arg: String ← Seq("-t", alias.toString)
    } yield arg
    Seq("docker", "build", "--force-rm") ++ dockerTagArgs :+ "."
  },
  Docker / dockerAliases := {
    val alias = (Docker / dockerAlias).value
    for (tag ← dockerTagsToPush.value) yield alias.withTag(Some(tag))
  }
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  ThisBuild / organization := "bandi.com",
  ThisBuild / scalaVersion := scalaVer,
  ThisBuild / resolvers := Seq(
    Resolver.mavenCentral,
    "shibboleth".at("https://build.shibboleth.net/nexus/content/repositories/releases/"),
    //    Resolver.mavenLocal,
    //    Resolver.jcenterRepo,
    "Atlassian's Maven Public Repository".at("https://packages.atlassian.com/maven-public/"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    "bandi2".at("https://bandi2.jfrog.io/artifactory/sbt-repo/")
  ),
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", _ @_*) ⇒ MergeStrategy.discard
    case PathList("reference.conf") ⇒ MergeStrategy.concat
    case "application.conf" ⇒ MergeStrategy.concat
    case _ ⇒ MergeStrategy.first
  },
  Test / fork := false,
  Compile / unmanagedResourceDirectories += file(".") / "./resources/main",
  Compile / unmanagedResourceDirectories += file(".") / "./resources/conf",
  Runtime / unmanagedResourceDirectories += file(".") / "./resources/main",
  Test / unmanagedResourceDirectories += file(".") / "./resources/test",
  // Coursier currently has problems with ignoring artifacts with packaging = pom,
  // which breaks e.g. commons-io
  // check: https://github.com/coursier/coursier/issues/278
  //  ThisBuild / useCoursier := false,
  ThisBuild / useJCenter := false,
  addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full)),
  scalacOptions += "-Ypartial-unification",
  // Disables documentation phase to speed up significantly incremental builds
  Compile / doc / sources := Seq.empty,
  Compile / packageDoc / publishArtifact := false,
  publish / skip := true,
  Docker / dockerRepository := dockerRepo
)

val commonSettingsScala213 = Seq(
  organization := "bandi.com",
  scalaVersion := "2.13.6",
  Compile / doc / sources := Seq.empty,
  Compile / packageDoc / publishArtifact := false,
  publish / skip := true,
  Docker / dockerRepository := dockerRepo,
  scalacOptions := scala213CompilerOptions,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*)                                 => MergeStrategy.discard
    case PathList("module-info.class")                                 => MergeStrategy.discard
    case dependency if dependency.contains("kafka-version.properties") => MergeStrategy.first
    case dependency if dependency.contains("yaml/snakeyaml")           => MergeStrategy.first
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)

dockerRepository in Docker in Global := dockerRepo
Global / lintUnusedKeysOnLoad := false

lazy val publishDockerImages = taskKey[Unit]("publish Docker Images")


lazy val runDockerPublish: ReleaseStep = ReleaseStep(
  action = { st: State ⇒
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    val st1 = extracted.runAggregated(ref / Docker / publish, st)
    extracted.runAggregated(ref / publishDockerImages, st1)
  }
)

def module(path: String): sbt.File = file(s"modules/$path")

lazy val deployment = (project in file("deployment")).settings(doNotPublish).settings(nonDockerSettings)
lazy val docs = (project in file("docs")).settings(doNotPublish).settings(nonDockerSettings)



javaOptions in Universal ++= Seq(
  // JVM memory tuning
  //  "-J-Xmx1024m",
  //  "-J-Xms512m",
  // Since play uses separate pidfile we have to provide it with a proper path
  // name of the pid file must be play.pid
  //   s"-Dpidfile.path=/var/run/${packageName.value}/play.pid",

  // alternative, you can remove the PID file
  s"-Dpidfile.path=/dev/null",
  // Use separate configuration file for production environment
  s"-Dconfig.file=/opt/docker/conf/application.conf",
  // Use separate logger configuration file for production environment
  // s"-Dlogger.file=/usr/share/${packageName.value}/conf/production-logger.xml",

  // You may also want to include this setting if you use play evolutions
  "-DapplyEvolutions.default=true",
  "-Dmail.smtp.ssl.protocols=TLSv1.2",
  "-Dlogger.file=/etc/logback/logback.xml",
  "-Dlogback.debug=true",
  "-Dhttp.port=9002"
)