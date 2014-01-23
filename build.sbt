organization := "com.typesafe.sbt"

name := "sbt-koan"

version := "0.1.0"

scalaVersion := Version.scala

libraryDependencies ++= Dependencies.sbtKoan

unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value)

unmanagedSourceDirectories in Test := List((scalaSource in Test).value)

scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

initialCommands := "import com.typesafe.sbt.sbtkoan._"
