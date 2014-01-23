organization := "com.typesafe.sbt"

name := "sbt-koan"

// version in version.sbt for sbt-release

// scalaVersion := Version.scala

libraryDependencies ++= Dependencies.sbtKoan

unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value)

unmanagedSourceDirectories in Test := List((scalaSource in Test).value)

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

initialCommands := "import com.typesafe.sbt.koan._"

sbtPlugin := true

publishTo := Some(if (isSnapshot.value) Classpaths.sbtPluginSnapshots else Classpaths.sbtPluginReleases)

publishMavenStyle := false
