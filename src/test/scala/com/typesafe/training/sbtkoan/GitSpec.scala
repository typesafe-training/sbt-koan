/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import java.io.File
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.{ Git => JGit }
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.scalatest.{ Matchers, WordSpec }
import scala.collection.JavaConversions._
import scala.sys.process.stringToProcess

class GitSpec extends WordSpec with Matchers {

  "Calling Git.checkoutPaths" should {
    "check out the given path" in {
      val f = fixture("checkoutPath")
      import f._
      git.checkoutPaths("b977596", "src/test") // Add Hello
      val srcTest = new File(repositoryPath, "src/test")
      contents(srcTest) map (_.getName) should contain theSameElementsAs List("HelloSpec.scala")
    }
    "check out the given paths" in {
      val f = fixture("checkoutPaths")
      import f._
      val srcMultiJvm = new File(repositoryPath, "src/multi-jvm")
      git.checkoutPaths("12c5fb0", "src/test") // Add multi-jvm test
      srcMultiJvm.exists() shouldEqual false
      git.checkoutPaths("12c5fb0", "src/test", "src/multi-jvm") // Add multi-jvm test
      contents(srcMultiJvm) map (_.getName) should contain theSameElementsAs List("KoanMultiJvm01.scala")
    }
  }

  "Calling Git.deletedOrRenamed" should {
    "return an empty sequence for a commit without any deleting or renaming" in {
      val f = fixture("checkoutPaths-withoutDeletingOrRenaming")
      import f._
      git.deletedOrRenamed("b977596", "5134987", "src/test") shouldBe 'empty // Add Hello
    }
    "return the paths of deleted or renamed entries" in {
      val f = fixture("checkoutPaths-withDeletingOrRenaming")
      import f._
      git.deletedOrRenamed("a5926e3", "e7d99d6", "src/test") map (_.getName) shouldEqual List("HelloSpec.scala") // Delete Hello
      git.deletedOrRenamed("b7bc4ee", "a5926e3", "src/test") map (_.getName) shouldEqual List("AddSpec.scala") // Rename Add to Plus
    }
  }

  def fixture(qualifier: String) =
    new {
      val dir = new File(FileUtils.getTempDirectory, qualifier)
      val repositoryPath = new File(dir, "koan")
      s"rm -r $repositoryPath".!
      s"unzip -qo -d $dir src/test/koan.zip".!
      val repository = (new FileRepositoryBuilder).setWorkTree(repositoryPath).build()
      val jgit = new JGit(repository)
      jgit.checkout.setName("koan").setCreateBranch(true).setStartPoint("5134987").call() // Initial state
      val git = new Git(repository)
    }

  def contents(dir: File): Seq[File] =
    FileUtils.listFiles(dir, null, true).toList
}
