/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import java.io.File
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{ Matchers, WordSpec }

class PackageSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {

  "Calling relativize" should {
    "return the relative path for a file contained in a parent directory" in {
      relativize(new File("/a/b"), new File("/a/b/c")) should ===("c")
      relativize(new File("/a/b"), new File("/a/b/c/d")) should ===("c/d")
      relativize(new File("/a/b/"), new File("/a/b/c/")) should ===("c")
    }
    "fail for a file not contained in a parent directory" in {
      an[IllegalArgumentException] should be thrownBy relativize(new File("/a/b"), new File("/a"))
      an[IllegalArgumentException] should be thrownBy relativize(new File("/a/b"), new File("/a/b"))
      an[IllegalArgumentException] should be thrownBy relativize(new File("/a/b"), new File("/a/c"))
    }
  }
}
