package com.tailrec.typeclass

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

trait NamespacedDifference {
  def namespace: Vector[String]

  def description: String = s"Difference at /${namespace.mkString("/")} \n    $information"

  def information: String

  def prependNamespace(n: String): NamespacedDifference
}

case class Difference(namespace: Vector[String], left: String, right: String) extends NamespacedDifference {
  def information: String = s"$left not equals $right"

  def prependNamespace(n: String): Difference = copy(namespace = n +: namespace)
}

case class SetDifference(namespace: Vector[String], leftOuter: Vector[String], rightOuter: Vector[String]) extends NamespacedDifference {
  def information: String =
    s"""
       |Elements found in left but not right: ${leftOuter.mkString(",")}
       |    Elements found in right but not left: ${rightOuter.mkString(",")}"
     """.stripMargin

  def prependNamespace(n: String): SetDifference = copy(namespace = n +: namespace)
}

case class Different(differences: Vector[NamespacedDifference]) extends DiffResult {
  override def description: String = differences.map(_.description).mkString("\n") + "\n"

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

trait GenericDiffLowPriorityImplicits extends DiffPrintLowPriorityImplicits {
  implicit def defaultDiff[A](implicit diffPrint: DiffPrint[A]): Diff[A] = Diff.build { (left, right) =>
    if (left == right) Identical else Different.fromPair(left, right)(diffPrint)
  }
}

trait GenericDiffImplicits extends GenericDiffLowPriorityImplicits with DiffPrintImplicits {

  implicit val hNilDiff: Diff[HNil] = Diff.build((_, _) => Identical)
  implicit val cNilDiff: Diff[CNil] = Diff.build((_, _) => Identical)

  implicit def hListDiff[K <: Symbol, H, T <: HList](implicit witness: Witness.Aux[K],
                                                     headDiff: Lazy[Diff[H]],
                                                     tailDiff: Diff[T]): Diff[FieldType[K, H] :: T] = Diff.build {
    case (leftHead :: leftTail, rightHead :: rightTail) =>
      headDiff.value(leftHead, rightHead).prependNamespace(witness.value.name) + tailDiff(leftTail, rightTail)
  }

  implicit def genericDiff[A, B](implicit generic: LabelledGeneric.Aux[A, B],
                                 diff: Lazy[Diff[B]]): Diff[A] = Diff.build { (left, right) =>
    diff.value(generic.to(left), generic.to(right))
  }


  implicit def coproductDiff[K <: Symbol, H, T <: Coproduct](implicit witness: Witness.Aux[K],
                                                             diffPrint: DiffPrint[FieldType[K, H] :+: T],
                                                             headDiff: Lazy[Diff[H]],
                                                             tailDiff: Diff[T]): Diff[FieldType[K, H] :+: T] = Diff.build {
    case (Inl(left), Inl(right)) => headDiff.value(left, right)
    case (Inr(left), Inr(right)) => tailDiff(left, right)
    case (left, right) => Different.fromPair(left, right)(diffPrint)
  }
}

object GenericDiffImplicits extends GenericDiffImplicits

