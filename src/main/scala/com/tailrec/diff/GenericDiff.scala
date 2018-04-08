package com.tailrec.diff

import shapeless._
import shapeless.labelled.FieldType

sealed trait DiffResult {
  def description: String

  def +(diffResult: DiffResult): DiffResult

  def prependNamespace(namespace: String): DiffResult
}

case object Identical extends DiffResult {
  override def description: String = "Identical"

  override def +(diffResult: DiffResult): DiffResult = diffResult

  override def prependNamespace(namespace: String): DiffResult = Identical
}

case class Difference(namespace: Vector[String], left: String, right: String) {
  def description: String = s"Difference found at ${namespace.mkString(".")} \n $left \n not equals \n $right \n"

  def prependNamespace(n: String) = copy(namespace = n +: namespace)
}

case class Different(differences: Vector[Difference]) extends DiffResult {
  override def description: String = differences.map(_.description).mkString(" \n")

  override def +(diffResult: DiffResult): DiffResult = diffResult match {
    case Identical => this
    case Different(d) => Different(differences ++ d)
  }

  override def prependNamespace(namespace: String): DiffResult = Different(differences.map(_.prependNamespace(namespace)))
}

object Different {
  def fromPair[A](left: A, right: A)(implicit diffPrint: DiffPrint[A]) = {
    Different(Vector(Difference(Vector.empty, diffPrint(left), diffPrint(right))))
  }
}

trait Diff[A] {
  def apply(left: A, right: A): DiffResult
}

object Diff {
  def build[A](f: (A, A) => DiffResult): Diff[A] = new Diff[A] {
    override def apply(left: A, right: A): DiffResult = f(left, right)
  }

  def apply[A](left: A, right: A)(implicit d: Diff[A]): DiffResult = d(left, right)
}

trait DiffPrint[A] {
  def apply(obj: A): String
}

object DiffPrint {
  def build[A](f: A => String): DiffPrint[A] = new DiffPrint[A] {
    override def apply(obj: A): String = f(obj)
  }
}

trait GenericDiffLowPriorityImplicits {
  implicit def defaultDiffPrint[A]: DiffPrint[A] = DiffPrint.build(_.toString)

  implicit def defaultDiff[A](implicit diffPrint: DiffPrint[A]): Diff[A] = Diff.build { (left, right) =>
    if (left == right) Identical else Different.fromPair(left, right)
  }
}

trait GenericDiff extends GenericDiffLowPriorityImplicits {
  implicit val hNilDiff: Diff[HNil] = Diff.build((_, _) => Identical)

  implicit def hListDiff[K <: Symbol, H, T <: HList](implicit witness: Witness.Aux[K],
                                                     headDiff: Lazy[Diff[H]],
                                                     tailDiff: Diff[T]): Diff[FieldType[K, H] :: T] = Diff.build {
    case (leftHead :: leftTail, rightHead :: rightTail) =>
      headDiff.value(leftHead, rightHead).prependNamespace(witness.value.name) + tailDiff(leftTail, rightTail)
  }

  implicit def caseClassDiff[A, B](implicit generic: LabelledGeneric.Aux[A, B],
                                   diff: Lazy[Diff[B]]): Diff[A] = Diff.build { (left, right) =>
    diff.value(generic.to(left), generic.to(right))
  }
}

object GenericDiff extends GenericDiff

