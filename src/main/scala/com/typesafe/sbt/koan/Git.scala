/*
 * Copyright 2013 Heiko Seeberger
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

import java.io.File
import org.eclipse.jgit.api.{ Git => JGit }
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType.{ DELETE, RENAME }
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.{ AbstractTreeIterator, CanonicalTreeParser }
import scala.collection.JavaConversions.iterableAsScalaIterable

object Git {
  def apply(workTree: File): Git =
    new Git((new FileRepositoryBuilder).setWorkTree(workTree).build())
}

class Git(repository: Repository) {

  private val jgit = new JGit(repository)

  def checkoutPaths(startPoint: String, path: String, paths: String*): Unit = {
    val singlePathCommand = jgit.checkout setStartPoint startPoint addPath path
    val command = (singlePathCommand /: paths)(_ addPath _)
    command.call()
  }

  def deletedOrRenamed(newRef: String, oldRef: String, path: String, paths: String*): Seq[File] = {
    def tree(ref: String): AbstractTreeIterator = {
      val reader = repository.newObjectReader
      try {
        val parser = new CanonicalTreeParser
        val tree = {
          val walk = new RevWalk(repository)
          walk.parseTree(walk.parseCommit(repository.resolve(ref)).getTree.getId)
        }
        parser.reset(reader, tree.getId)
        parser
      } finally reader.release()
    }
    def matches(entry: DiffEntry) = {
      val allPaths = path +: paths.toList
      (entry.getChangeType == DELETE || entry.getChangeType == RENAME) &&
        (allPaths exists (entry.getOldPath startsWith _))
    }
    val command = jgit.diff setNewTree tree(newRef) setOldTree tree(oldRef)
    command.call().toList collect { case entry if matches(entry) => new File(entry.getOldPath) }
  }

  def history(ref: String): Seq[(String, String)] = {
    val command = jgit.log add repository.resolve(ref)
    command.call().toList map (commit => commit.shortId -> commit.getShortMessage)
  }

  def reset(path: String, paths: String*): Unit = {
    val singlePathCommand = jgit.reset addPath path
    val command = (singlePathCommand /: paths)(_ addPath _)
    command.call()
  }
}
