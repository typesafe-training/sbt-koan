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

package com.typesafe.sbt

import java.io.File
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.revwalk.RevCommit
import sbt.{ BuildStructure, Extracted, Project, SettingKey, State, ThisProject }
import scala.annotation.tailrec

package object koan {

  type Traversable[+A] = scala.collection.immutable.Traversable[A]

  type Iterable[+A] = scala.collection.immutable.Iterable[A]

  type Seq[+A] = scala.collection.immutable.Seq[A]

  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]

  implicit class RevCommitOps(commit: RevCommit) {
    def shortId: String =
      (commit abbreviate 7).name
  }

  def fst[A, B](pair: (A, B)): A =
    pair._1

  def setting[A](key: SettingKey[A], state: State) =
    key in ThisProject get structure(state).data getOrElse sys.error(s"Fatal: sbt setting '$key' undefined!")

  def structure(state: State): BuildStructure =
    extracted(state).structure

  def extracted(state: State): Extracted =
    Project extract state
}
