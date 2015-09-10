/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import java.io.File
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api.{ Git => JGit }
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.{ RefSpec, TagOpt, UsernamePasswordCredentialsProvider }
import org.eclipse.jgit.treewalk.{ AbstractTreeIterator, CanonicalTreeParser }
import scala.collection.JavaConversions._

object Git {
  def apply(workTree: File): Git =
    new Git((new FileRepositoryBuilder).setWorkTree(workTree).build())
}

class Git(repository: Repository) {

  private val jgit = new JGit(repository)

  private val credentialsProvider =
    new UsernamePasswordCredentialsProvider("koan-api", "aut0mat1zat10n")

  def checkoutPaths(startPoint: String, path: String, paths: String*): Unit = {
    val singlePathCommand =
      jgit.checkout
        .setStartPoint(startPoint)
        .addPath(path)
    val command = (singlePathCommand /: paths)(_.addPath(_))
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
      } finally
        reader.close()
    }
    def matches(entry: DiffEntry) = {
      val allPaths = path +: paths.toList
      (entry.getChangeType == ChangeType.DELETE || entry.getChangeType == ChangeType.RENAME) &&
        allPaths.exists(entry.getOldPath.startsWith)
    }
    jgit.diff
      .setNewTree(tree(newRef))
      .setOldTree(tree(oldRef))
      .call()
      .toList
      .collect { case entry if matches(entry) => new File(entry.getOldPath) }
  }

  def fetch(remote: String, ref: String) =
    jgit.fetch
      .setRemote(remote)
      .setRefSpecs(new RefSpec(s"+refs/heads/$ref:refs/remotes/origin/$ref"))
      .setCredentialsProvider(credentialsProvider)
      .setTagOpt(TagOpt.NO_TAGS)
      .call()

  def history(ref: String): Seq[(String, String)] = {
    jgit.log
      .add(repository.resolve(ref))
      .call()
      .toList
      .map(commit => commit.shortId -> commit.getShortMessage)
  }

  def reset(path: String, paths: String*): Unit = {
    val singlePathCommand = jgit.reset.addPath(path)
    val command = (singlePathCommand /: paths)(_ addPath _)
    command.call()
  }

  def resetHard(ref: String): Unit =
    jgit.reset
      .setRef(ref)
      .setMode(ResetType.HARD)
      .call()
}
