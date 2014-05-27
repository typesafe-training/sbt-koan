lazy val sbtKoan = project in file(".")

name := "sbt-koan"

Common.settings

libraryDependencies ++= Dependencies.sbtKoan

initialCommands := """|import com.typesafe.training.sbtkoan._""".stripMargin
