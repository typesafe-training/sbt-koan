/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training

import java.io.File
import java.nio.charset.Charset
import org.apache.commons.io.FilenameUtils
import org.eclipse.jgit.revwalk.RevCommit
import sbt.{ BuildStructure, Configuration, Extracted, Project, SettingKey, State, ThisProject }

package object sbtkoan {

  type Traversable[+A] = scala.collection.immutable.Traversable[A]

  type Iterable[+A] = scala.collection.immutable.Iterable[A]

  type Seq[+A] = scala.collection.immutable.Seq[A]

  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  implicit class RevCommitOps(commit: RevCommit) {
    def shortId: String =
      (commit abbreviate 7).name
  }

  val utf8: Charset =
    Charset.forName("utf-8")

  def fst[A, B](pair: (A, B)): A =
    pair._1

  def resolve(parent: File)(child: File): Option[String] = {
    def elements(file: File) = {
      val path =
        FilenameUtils.separatorsToUnix(file.getCanonicalPath) match {
          case s if s startsWith "/" => s substring 1
          case s                     => s
        }
      path split "/" toList
    }
    val parentElements = elements(parent)
    val childElements = elements(child)
    if (childElements startsWith parentElements)
      childElements drop parentElements.size match {
        case Nil      => None
        case elements => Some(elements mkString "/")
      }
    else
      None
  }

  def setting[A](key: SettingKey[A], state: State) =
    key in ThisProject get structure(state).data getOrElse sys.error(s"Fatal: sbt setting '$key' undefined!")

  def setting[A](key: SettingKey[A], config: Configuration, state: State) =
    key in (ThisProject, config) get structure(state).data getOrElse sys.error(s"Fatal: sbt setting '$key' in '$config' undefined!")

  def structure(state: State): BuildStructure =
    extracted(state).structure

  def extracted(state: State): Extracted =
    Project extract state
}
