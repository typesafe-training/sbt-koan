/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training

import java.io.File
import java.nio.charset.Charset
import org.apache.commons.io.FilenameUtils
import org.eclipse.jgit.revwalk.RevCommit

package object sbtkoan {

  type Traversable[+A] = scala.collection.immutable.Traversable[A]

  type Iterable[+A] = scala.collection.immutable.Iterable[A]

  type Seq[+A] = scala.collection.immutable.Seq[A]

  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  implicit class StringOps(val s: String) extends AnyVal {
    def decapitalize: String = {
      if (s == null)
        null
      else if (s.isEmpty)
        s
      else
        s.head.toLower +: s.tail
    }
  }

  implicit class RevCommitOps(commit: RevCommit) {
    def shortId: String =
      commit.abbreviate(7).name
  }

  val utf8: Charset =
    Charset.forName("utf-8")

  def fst[A, B](pair: (A, B)): A =
    pair._1

  def relativize(parent: File, child: File): String = {
    def elements(file: File) = {
      val path =
        FilenameUtils.separatorsToUnix(file.getCanonicalPath) match {
          case s if s.startsWith("/") => s.substring(1)
          case s                      => s
        }
      path.split("/").toList
    }
    val parentElements = elements(parent)
    val childElements = elements(child)
    if (childElements.startsWith(parentElements))
      childElements.drop(parentElements.size) match {
        case Nil      => throw new IllegalArgumentException(s"$child is not a child of $parent!")
        case elements => elements.mkString("/")
      }
    else
      throw new IllegalArgumentException(s"$child is not a child of $parent!")
  }
}
