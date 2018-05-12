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

  def description: String = s"Difference at /${namespace.mkString("/")}\n$information"

  def information: String

  def prependNamespace(n: String): NamespacedDifference
}

case class Difference(namespace: Vector[String], left: String, right: String) extends NamespacedDifference {
  def information: String = s"    $left not equals $right"

  def prependNamespace(n: String): Difference = copy(namespace = n +: namespace)
}

case class SetDifference(namespace: Vector[String], leftOuter: Set[String], rightOuter: Set[String]) extends NamespacedDifference {
  def information: String = {
    leftOuter.headOption.fold("")(_ => s"    In left but not right: ${leftOuter.mkString(", ")}\n") +
      rightOuter.headOption.fold("")(_ => s"    In right but not left: ${rightOuter.mkString(", ")}")
  }

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
  def fromPair[A](left: A, right: A)(implicit diffPrint: DiffPrint[A]): Different = {
    Different(Vector(Difference(Vector.empty, diffPrint(left), diffPrint(right))))
  }

  def fromSets[A](left: Set[A], right: Set[A])(implicit diffPrint: DiffPrint[A]): Different = {
    Different(Vector(SetDifference(Vector.empty, left.map(diffPrint.apply), right.map(diffPrint.apply))))
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

trait DiffImplicits0 extends DiffPrintImplicits {
  implicit def defaultDiff[A](implicit diffPrint: DiffPrint[A]): Diff[A] = Diff.build { (left, right) =>
    //    implicit def defaultDiff(implicit diffPrint: DiffPrint[Int]): Diff[Int] = Diff.build { (left, right) =>
    if (left == right) Identical else Different.fromPair(left, right)(diffPrint)
  }
}

trait DiffImplicits1 extends DiffImplicits0 {

  implicit val hNilDiff: Diff[HNil] = Diff.build((_, _) => Identical)
  implicit val cNilDiff: Diff[CNil] = Diff.build((_, _) => throw new RuntimeException("unexpected CNil"))

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

trait DiffImplicits extends DiffImplicits1 {
  implicit def setDiff[A, B](implicit ev: B <:< Set[A],
                             diff: Diff[A],
                             diffPrint: DiffPrint[A]): Diff[B] = Diff.build { (left, right) =>
    val leftNotRight = left.filterNot(l => right.exists(r => diff(l, r) == Identical))
    val rightNotLeft = right.filterNot(r => left.exists(l => diff(r, l) == Identical))
    if (left.nonEmpty || right.nonEmpty) Different.fromSets(leftNotRight, rightNotLeft) else Identical
  }

  implicit def orderedDiff[A, B](implicit ev: B <:< Seq[A],
                                 diff: Diff[A],
                                 diffPrint: DiffPrint[A]): Diff[B] = Diff.build { (left, right) =>
    val indexDifferences = left
      .zipWithIndex
      .zip(right)
      .map {
        case ((leftElement, index), rightElement) => diff(leftElement, rightElement).prependNamespace(s"[$index]")
      }
      .fold(Identical)(_ + _)

    val existDifferences = if (left.size != right.size) {
      val minIndex = math.min(left.size, right.size)
      Different
        .fromSets(left.slice(minIndex, left.size).toSet, right.slice(minIndex, right.size).toSet)
        .prependNamespace("#")
    } else Identical

    indexDifferences + existDifferences
  }
}

object DiffImplicits extends DiffImplicits

trait UnorderedDiffImplicits extends DiffImplicits {
  implicit def unorderedIterableDiff[A, B](implicit ev: B <:< Iterable[A],
                                           diff: Diff[A],
                                           diffPrint: DiffPrint[A]): Diff[B] = Diff.build { (left, right) =>
    setDiff[A, Set[A]].apply(left.toSet, right.asInstanceOf[Iterable[A]].toSet)
  }
}

object UnorderedDiffImplicits extends UnorderedDiffImplicits
