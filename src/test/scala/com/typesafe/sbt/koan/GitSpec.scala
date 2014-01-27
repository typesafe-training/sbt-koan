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
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.{ Git => JGit }
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.scalatest.{ Matchers, WordSpec }
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.sys.process.stringToProcess

class GitSpec extends WordSpec with Matchers {

  "Calling Git.checkoutPaths" should {
    "check out the given paths" in {
      val f = fixture("checkoutPaths")
      import f._
      git.checkoutPaths("20cf1a8", "src/test") // Add Hello
      val srcTest = new File(repositoryPath, "src/test")
      contents(srcTest) map (_.getName) should contain theSameElementsAs List("HelloSpec.scala")
    }
  }

  "Calling Git.deletedOrRenamed" should {
    "return an empty sequence for a commit without any deleting or renaming" in {
      val f = fixture("checkoutPaths-withoutDeletingOrRenaming")
      import f._
      git.deletedOrRenamed("20cf1a8", "da99316", "src/test") shouldBe 'empty // Add Hello
    }
    "return the paths of deleted or renamed entries" in {
      val f = fixture("checkoutPaths-withDeletingOrRenaming")
      import f._
      git.deletedOrRenamed("1b5a471", "68a92b6", "src/test") map (_.getName) shouldEqual List("HelloSpec.scala") // Delete Hello
      git.deletedOrRenamed("a56e289", "1b5a471", "src/test") map (_.getName) shouldEqual List("AddSpec.scala") // Rename Add to Plus
    }
  }

  def fixture(qualifier: String) =
    new {
      val dir = new File(FileUtils.getTempDirectory, qualifier)
      val repositoryPath = new File(dir, s"koan")
      s"rm -r $repositoryPath".!
      s"unzip -qo -d $dir src/test/koan.zip".!
      val repository = (new FileRepositoryBuilder).setWorkTree(repositoryPath).build()
      val jgit = new JGit(repository)
      jgit.checkout.setName("koan").setCreateBranch(true).setStartPoint("da99316").call() // Initial state
      val git = new Git(repository)
    }

  def contents(dir: File): Seq[File] =
    FileUtils.listFiles(dir, null, true).toList
}
