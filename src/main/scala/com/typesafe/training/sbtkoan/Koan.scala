/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import SbtKoan.autoImport
import java.io.{ IOException, File, FileInputStream, FileOutputStream }
import org.apache.commons.io.FileUtils
import sbt.{ Keys, State }

private object Koan {
  def apply(state: State, koanArg: KoanArg): State =
    new Koan(state, koanArg)()
}

private class Koan(state: State, koanArg: KoanArg) {

  val baseDirectory = setting(Keys.baseDirectory, state)

  val testDirectories = setting(autoImport.configurations, state) map (c => setting(Keys.sourceDirectory, c, state))

  val historyRef = setting(autoImport.historyRef, state)

  val initial = setting(autoImport.initial, state)

  val ignore = setting(autoImport.ignore, state)

  val tag = FileUtils.readFileToString(new File(baseDirectory, ".tag"), utf8).trim

  val testPath :: testPaths = testDirectories.toList flatMap resolve(baseDirectory)

  val git = Git(baseDirectory)

  val (koans, koanMessages) = {
    val koans = git.history(historyRef).reverse dropWhile { case (_, message) => !(message contains initial) }
    if (koans.isEmpty)
      sys.error(s"Fatal: Initial state not in Git history, i.e. no commit contains '$initial'!")
    else
      (koans map fst, koans.toMap)
  }

  val current = findCurrent() getOrElse koans.head

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
    val otherQualifier = if (forward) "next" else "previous"
    (if (forward) koans else koans.reverse) dropWhile (_ != current) filterNot (koanMessages(_) contains ignore) match {
      case Nil =>
        state.log.error(s"Can't move to $otherQualifier koan, because invalid current id '$current'!")
        state.log.error(s"Hint: Try to delete ${koanProperties.getCanonicalPath} and preferably run `git clean -df`")
        state.fail
      case _ +: Nil =>
        val end = if (forward) "last koan" else "initial state"
        state.log.warn(s"Already arrived at $end!")
        state
      case _ +: other +: _ =>
        git.checkoutPaths(other, testPath, testPaths: _*)
        git.deletedOrRenamed(other, current, testPath, testPaths: _*) foreach FileUtils.forceDelete
        git.reset(testPath, testPaths: _*)
        saveCurrent(other)
        state.log.info(s"Moved to $otherQualifier koan '${koanMessages(other)}'")
        state
    }
  }

  def pullSolutions(): State = {
    git.fetch("origin", s"$tag-solutions")
    git.resetHard(s"origin/$tag-solutions")
    state.log.info(s"Pulled solutions into workspace")
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
    } finally out.close()
  }

  def koanProperties: File =
    new File(baseDirectory, ".koan.properties")
}
