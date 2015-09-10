import sbt._

object Version {
  val commonsIO = "2.4"
  val jGit      = "4.0.1.201506240215-r"
  val scala     = "2.10.4"
  val scalaTest = "2.2.2"
}

object Library {
  val commonsIO = "commons-io"       % "commons-io"       % Version.commonsIO
  val jGit      = "org.eclipse.jgit" % "org.eclipse.jgit" % Version.jGit
  val scalaTest = "org.scalatest"    %% "scalatest"       % Version.scalaTest
}

object Dependencies {

  import Library._

  val sbtKoan = List(
    commonsIO,
    jGit,
    scalaTest % "test"
  )
}
