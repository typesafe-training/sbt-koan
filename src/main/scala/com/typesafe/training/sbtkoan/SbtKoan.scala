/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

import sbt.{ Command, Configuration, Configurations, Keys, Plugin, Setting, SettingKey, State }
import sbt.complete.Parser

object SbtKoan extends Plugin {

  object KoanKey {

    val configurations: SettingKey[Set[Configuration]] =
      SettingKey[Set[Configuration]](
        prefixed("configurations"),
        """The configurations determine the considered source directories; Set(Configurations.Test) by default"""
      )

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

    val historyRef: SettingKey[String] =
      SettingKey[String](
        prefixed("historyRef"),
        """The ref (commit id, branch or tag) used for the Git history; "koan" by default"""
      )

    private def prefixed(key: String) = "koan" + key.capitalize
  }

  override def settings: Seq[Setting[_]] =
    List(
      Keys.commands += koanCommand,
      KoanKey.configurations := Set(Configurations.Test),
      KoanKey.historyRef := "koan",
      KoanKey.initial := "koan:initial",
      KoanKey.ignore := "koan:ignore"
    )

  private def koanCommand =
    Command("koan")(parser)(Koan.apply)

  private def parser(state: State) = {
    import KoanArg._
    import sbt.complete.DefaultParsers._
    def arg(koanArg: KoanArg): Parser[KoanArg] = {
      (Space ~> koanArg.toString.toLowerCase) map (_ => koanArg)
    }
    arg(Show) | arg(Next) | arg(Prev) | arg(Solutions)
  }
}
