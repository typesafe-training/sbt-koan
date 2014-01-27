/*
 * Copyright 2014 Typesafe Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.typesafe.sbt.koan

import java.io.{ IOException, File, FileInputStream, FileOutputStream }
import org.apache.commons.io.FileUtils
import sbt.State

private object Koan {

  val buildDefinition = """.+sbt|project/.+\.scala""".r

  object Action {
    def apply(baseDirectory: File, initialState: String, state: State) =
      new Action(baseDirectory, initialState, state)
  }

  class Action(baseDirectory: File, initialState: String, state: State) {

    val git = Git(baseDirectory)

    val (koans, koanMessages) = {
      val koans = git.history("master").reverse dropWhile { case (_, message) => message != initialState }
      if (koans.isEmpty)
        sys.error(s"Fatal: Initial state '$initialState' not in Git history!")
      else
        (koans map fst, koans.toMap)
    }

    val current = findCurrent() getOrElse koans.head

    def apply(koanOpt: KoanOpt) = {
      import KoanOpt._
      koanOpt match {
        case Next => move(forward = true)
        case Prev => move(forward = false)
        case Show => show()
      }
    }

    def move(forward: Boolean) = {
      val otherQualifier = if (forward) "next" else "previous"
      (if (forward) koans else koans.reverse) dropWhile (_ != current) match {
        case Nil =>
          state.log.error(s"Can't move to $otherQualifier koan, because invalid current id '$current'!")
          state.log.error(s"Hint: Try to delete ${koanProperties.getCanonicalPath} and preferably run `git clean -df`")
          state.fail
        case _ +: Nil =>
          val end = if (forward) "last koan" else "initial state"
          state.log.warn(s"Already arrived at $end!")
          state
        case _ +: other +: _ =>
          git.checkoutPaths(other, "src/test")
          git.deletedOrRenamed(other, current, "src/test") foreach FileUtils.forceDelete
          git.reset("src/test")
          saveCurrent(other)
          state.log.info(s"Moved to $otherQualifier koan '${koanMessages(other)}'")
          state
      }
    }

    def show() = {
      state.log.info(s"Currently at koan '${koanMessages(current)}'")
      state
    }

    def findCurrent() = {
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

    def saveCurrent(current: String) = {
      val properties = new java.util.Properties
      properties.setProperty("current", current)
      val out = new FileOutputStream(koanProperties)
      try {
        properties.store(out, null)
      } finally out.close()
    }

    def koanProperties = new File(baseDirectory, ".koan.properties")
  }
}
