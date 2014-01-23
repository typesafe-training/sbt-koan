import sbt._

object Version {
  // val scala     = "2.10.3"
  val commonsIO = "2.4"
  val jGit      = "3.1.0.201310021548-r"
  val scalaTest = "2.0"
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
