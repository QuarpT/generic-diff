package com.tailrec.typeclass

import shapeless._
import shapeless.labelled.FieldType

trait DiffPrint[A] {
  def apply(obj: A): String
}

object DiffPrint {
  def apply[A](obj: A)(implicit p: DiffPrint[A]): String = p(obj)
  def build[A](f: A => String): DiffPrint[A] = new DiffPrint[A] {
    override def apply(obj: A): String = f(obj)
  }
}

trait DiffPrintDefaultImplicits {
  implicit def defaultDiffPrint[A]: DiffPrint[A] = {
    DiffPrint.build(_.toString)
  }
}

trait DiffPrintLowPriorityImplicits extends DiffPrintDefaultImplicits {
  implicit def genericDiffPrint[A, B](implicit generic: Generic.Aux[A, B],
                                      className: ClassName[A],
                                      diffPrint: Lazy[DiffPrint[B]]): DiffPrint[A] = DiffPrint.build { obj =>
    s"${className()}(${diffPrint.value(generic.to(obj))})"
  }
}

trait DiffPrintImplicits extends DiffPrintLowPriorityImplicits {
  implicit val hNilDiffPrint: DiffPrint[HNil] = DiffPrint.build(_ => "")
  implicit val cNilDiffPrint: DiffPrint[CNil] = DiffPrint.build(_ => throw new RuntimeException("unexpected CNil"))

  implicit def hListDiffPrint[A, B, T <: HList](implicit headDiffPrint: Lazy[DiffPrint[A]],
                                                tailDiffPrint: DiffPrint[B :: T]): DiffPrint[A :: (B :: T)] = DiffPrint.build { hlist =>
    s"${headDiffPrint.value(hlist.head)},${tailDiffPrint(hlist.tail)}"
  }

  implicit def hListDiffPrintLast[A](implicit headDiffPrint: Lazy[DiffPrint[A]]): DiffPrint[A :: HNil] = DiffPrint.build { hlist =>
    s"${headDiffPrint.value(hlist.head)}"
  }

  implicit def genericCoproductDiffPrint[A, B <: Coproduct](implicit generic: Generic.Aux[A, B],
                                                            diffPrint: Lazy[DiffPrint[B]]): DiffPrint[A] = DiffPrint.build { obj =>
    s"${diffPrint.value(generic.to(obj))}"
  }

  implicit def coproductDiffPrint[H, T <: Coproduct](implicit headDiffPrint: Lazy[DiffPrint[H]],
                                                     tailDiffPrint: DiffPrint[T]): DiffPrint[H :+: T] = DiffPrint.build {
    case Inl(head) => headDiffPrint.value(head)
    case Inr(tail) => tailDiffPrint(tail)
  }

  implicit def witnessedCoproductDiffPrint[K <: Symbol, H, T <: Coproduct](implicit witness: Witness.Aux[K],
                                                                           headDiffPrint: Lazy[DiffPrint[H]],
                                                                           tailDiffPrint: DiffPrint[T]): DiffPrint[FieldType[K, H] :+: T] = DiffPrint.build {
    case Inl(head) => headDiffPrint.value(head)
    case Inr(tail) => tailDiffPrint(tail)
  }
}

object DiffPrintImplicits extends DiffPrintImplicits
