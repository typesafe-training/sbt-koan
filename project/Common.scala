import com.typesafe.sbt.SbtScalariform._
import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin._
import scalariform.formatter.preferences._

object Common {

  val settings =
    scalariformSettings ++
    releaseSettings ++
    List(
      // Core settings
      organization := "com.typesafe.training",
      scalaVersion := Version.scala,
      crossScalaVersions := List(scalaVersion.value),
      scalacOptions ++= List(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-target:jvm-1.6",
        "-encoding", "UTF-8"
      ),
      unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value),
      unmanagedSourceDirectories in Test := List((scalaSource in Test).value),
      sbtPlugin := true,
      // Publish settings
      publishTo := typesafeIvyRepo(isSnapshot.value),
      publishMavenStyle := false,
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in (Compile, packageSrc) := false,
      // Scalariform settings
      ScalariformKeys.preferences := ScalariformKeys.preferences.value
        .setPreference(AlignArguments, true)
        .setPreference(AlignParameters, true)
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        .setPreference(DoubleIndentClassDeclaration, true),
      // Release settings
      ReleaseKeys.versionBump := sbtrelease.Version.Bump.Minor
    )

  def typesafeIvyRepo(isSnapshot: Boolean) = {
    val (name, url) =
      if (isSnapshot)
        "ivy-snapshots" -> "http://private-repo.typesafe.com/typesafe/ivy-snapshots/"
      else
        "ivy-releases" -> "http://private-repo.typesafe.com/typesafe/ivy-releases/"
    Some(Resolver.url(name, new java.net.URL(url))(Resolver.ivyStylePatterns))
  }
}
