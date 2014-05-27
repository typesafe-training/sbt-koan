import sbt._

object Version {
  val commonsIO = "2.4"
  val jGit      = "3.3.2.201404171909-r"
  val scala     = "2.10.4"
  val scalaTest = "2.1.7"
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
