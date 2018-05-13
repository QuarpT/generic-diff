package com.tailrec.typeclass

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

trait ClassName[A] {
  def apply(): String
}

object ClassName {
  def apply[A](implicit className: ClassName[A]): String = className()

  implicit def materialize[A]: ClassName[A] = macro materializeImpl[A]

  def materializeImpl[A: c.WeakTypeTag](c: whitebox.Context): c.Tree = {
    import c.universe._
    q"""new ClassName[${c.weakTypeOf[A]}] {
      override def apply(): String = ${c.weakTypeOf[A].typeSymbol.name.decodedName.toString}
    }"""
  }
}
