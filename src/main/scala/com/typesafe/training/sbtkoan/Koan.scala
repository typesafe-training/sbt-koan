/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import SbtKoan.autoImport
import java.io.{ File, FileInputStream, FileOutputStream, IOException }
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.errors.GitAPIException
import sbt.{ Configuration, Keys, Project, State }

private object Koan {

  def apply(state: State, koanArg: KoanArg): State =
    new Koan(state, koanArg).apply()

  def allSourceDirectories(config: Configuration, state: State): Seq[String] = {
    val buildDirectory = Project.extract(state).get(Keys.baseDirectory)
    Project.extract(state).structure.allProjectRefs.toList.map { ref =>
      val sourceDirectory = Project.extract(state).get(Keys.sourceDirectory in config in ref)
      relativize(buildDirectory, sourceDirectory)
    }
  }
}

private class Koan(state: State, koanArg: KoanArg) {

  import Koan._

  val baseDirectory = Project.extract(state).get(Keys.baseDirectory)

  val testDirectories = Project.extract(state).get(autoImport.configurations).flatMap(c => allSourceDirectories(c, state))

  val historyRef = Project.extract(state).get(autoImport.historyRef)

  val initial = Project.extract(state).get(autoImport.initial)

  val ignore = Project.extract(state).get(autoImport.ignore)

  val tag = FileUtils.readFileToString(new File(baseDirectory, ".tag"), utf8).trim

  val testPath :: testPaths = testDirectories.toList

  val git = Git(baseDirectory)

  val (koans, koanMessages) = {
    val koans = git.history(historyRef).reverse.dropWhile { case (_, message) => !(message contains initial) }
    if (koans.isEmpty)
      sys.error(s"Fatal: Initial state not in Git history, i.e. no commit contains '$initial'!")
    else
      (koans.map(fst), koans.toMap)
  }

  val current = findCurrent().getOrElse(koans.head)

  def apply(): State = {
    import KoanArg._
    koanArg match {
      case Show          => show()
      case Next          => move(forward = true)
      case Prev          => move(forward = false)
      case PullSolutions => pullSolutions()
    }
  }

  def show(): State = {
    state.log.info(s"Currently at koan '${koanMessages(current)}'")
    state
  }

  def move(forward: Boolean): State = {
    val (otherQualifier, theseKoans) =
      if (forward)
        ("next", koans)
      else
        ("previous", koans.reverse)
    theseKoans.dropWhile(_ != current).filterNot(koanMessages(_).contains(ignore)) match {
      case Nil =>
        state.log.error(s"Can't move to $otherQualifier koan, because invalid current id '$current'!")
        state.log.error(s"Hint: Try to delete ${koanProperties.getCanonicalPath} and preferably run `git clean -df`")
        state.fail
      case _ +: Nil =>
        val end =
          if (forward)
            "last koan"
          else
            "initial state"
        state.log.warn(s"Already arrived at $end!")
        state
      case _ +: other +: _ =>
        git.checkoutPaths(other, testPath, testPaths: _*)
        git.deletedOrRenamed(other, current, testPath, testPaths: _*).foreach(FileUtils.forceDelete)
        git.reset(testPath, testPaths: _*)
        saveCurrent(other)
        state.log.info(s"Moved to $otherQualifier koan '${koanMessages(other)}'")
        state
    }
  }

  def pullSolutions(): State = {
    try {
      git.fetch("origin", s"$tag-solutions")
      git.resetHard(s"origin/$tag-solutions")
      state.log.info(s"Pulled solutions into workspace")
    } catch {
      case e: GitAPIException => state.log.error(s"Can't pull solutions: ${e.getMessage}")
    }
    state
  }

  def findCurrent(): Option[String] = {
    val properties = new java.util.Properties
    try {
      val in = new FileInputStream(koanProperties)
      properties.load(in)
      in.close()
      Option(properties.getProperty("current"))
    } catch {
      case _: IOException => None
    }
  }

  def saveCurrent(current: String): Unit = {
    val properties = new java.util.Properties
    properties.setProperty("current", current)
    val out = new FileOutputStream(koanProperties)
    try {
      properties.store(out, null)
    } finally
      out.close()
  }

  def koanProperties: File =
    new File(baseDirectory, ".koan.properties")
}
