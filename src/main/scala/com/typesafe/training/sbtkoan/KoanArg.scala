/*
 * Copyright 2014 Typesafe
 */

package com.typesafe.training.sbtkoan

private sealed trait KoanArg

private object KoanArg {
  case object Show extends KoanArg
  case object Next extends KoanArg
  case object Prev extends KoanArg
  case class Pull(branch: String) extends KoanArg
}
