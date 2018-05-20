package com.tailrec.typeclass

import org.scalatest.{FlatSpec, Matchers}

class DiffPrintTest extends FlatSpec with Matchers with DiffPrintImplicits {
  it should "print ints using default to string" in {
    DiffPrint(55) shouldBe "55"
  }

  it should "print lists" in {
    DiffPrint(List(1, 2, 3)) shouldBe "Iterable(1,2,3)"
  }

  it should "print case classes" in {
    DiffPrint(Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty)))) shouldBe
      "Man(John,54,Iterable(Woman(Jess,28,Iterable(),Iterable())),Iterable(Man(Joe,4,Iterable(),Iterable())))"
  }

  it should "print coproducts" in {
    DiffPrint[Human](Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty)))) shouldBe
      "Man(John,54,Iterable(Woman(Jess,28,Iterable(),Iterable())),Iterable(Man(Joe,4,Iterable(),Iterable())))"
  }

  it should "print case objects" in {
    DiffPrint(Baby) shouldBe "Baby()"
  }
}
