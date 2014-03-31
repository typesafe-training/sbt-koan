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

import sbt.{ Command, Configurations, Keys, Plugin, Setting, SettingKey, State }
import sbt.complete.Parser

object SbtKoan extends Plugin {

  object KoanKeys {

    val initial: SettingKey[String] =
      SettingKey[String](
        prefixed("initial"),
        """If a commit message contains this text, the commit is treated as the initial state; "koan:initial" by default"""
      )

    val ignore: SettingKey[String] =
      SettingKey[String](
        prefixed("ignore"),
        """If a commit message contains this text, the commit is ignored; "koan:ignore" by default"""
      )

    private def prefixed(key: String) = "koan" + key.capitalize
  }

  override def settings: Seq[Setting[_]] =
    List(
      Keys.commands += koanCommand,
      KoanKeys.initial := "koan:initial",
      KoanKeys.ignore := "koan:ignore"
    )

  private def koanCommand = {
    Command("koan")(parser) { (state, koanOpt) =>
      val baseDirectory = setting(Keys.baseDirectory, state)
      val testDirectory = setting(Keys.sourceDirectory, Configurations.Test, state)
      val initial = setting(KoanKeys.initial, state)
      val ignore = setting(KoanKeys.ignore, state)
      Koan.Action(baseDirectory, testDirectory, initial, ignore, state)(koanOpt)
    }
  }

  private def parser(state: State) = {
    import KoanOpt._
    import sbt.complete.DefaultParsers._
    def opt(koanOpt: KoanOpt): Parser[KoanOpt] = {
      (Space ~> koanOpt.toString.toLowerCase) map (_ => koanOpt)
    }
    opt(Show) | opt(Next) | opt(Prev)
  }
}
