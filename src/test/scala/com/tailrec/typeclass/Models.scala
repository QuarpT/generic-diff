package com.tailrec.typeclass

sealed trait Human

case class Man(name: String, age: Int, children: List[Human], grandChildren: Set[Human]) extends Human

case class Woman(name: String, age: Int, children: List[Human], grandChildren: Set[Human]) extends Human

case object Baby extends Human
