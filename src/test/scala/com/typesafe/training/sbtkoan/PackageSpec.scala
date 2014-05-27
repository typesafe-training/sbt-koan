/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import java.io.File
import org.scalatest.{ Matchers, WordSpec }

class PackageSpec extends WordSpec with Matchers {

  "Calling resolve" should {
    "return Some with the relative path for a file contained in a parent directory" in {
      resolve(new File("/a/b"))(new File("/a/b/c")) shouldEqual Some("c")
      resolve(new File("/a/b"))(new File("/a/b/c/d")) shouldEqual Some("c/d")
      resolve(new File("/a/b/"))(new File("/a/b/c/")) shouldEqual Some("c")
    }
    "return None for a file not contained in a parent directory" in {
      resolve(new File("/a/b"))(new File("/a")) shouldEqual None
      resolve(new File("/a/b"))(new File("/a/b")) shouldEqual None
      resolve(new File("/a/b"))(new File("/a/c")) shouldEqual None
    }
  }
}
