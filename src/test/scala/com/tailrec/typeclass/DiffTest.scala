package com.tailrec.typeclass

import org.scalatest.{FlatSpec, Matchers}

class DiffTest extends FlatSpec with Matchers {

  it should "diff ints falling back to default diff" in new DiffImplicits {
    Diff(5, 5) shouldBe Identical
    Diff(5, 6) shouldBe Different(Vector(Difference(Vector.empty, "5", "6")))
    Diff(5, 6).toString shouldBe
      """|Difference at /
         |    5 not equals 6
         |
         |""".stripMargin
  }

  it should "diff sets" in new DiffImplicits {
    Diff(Set(1, 2, 3), Set(1, 2, 3)) shouldBe Identical
    Diff(Set(1, 2, 4), Set(1, 3)) shouldBe Different(Vector(SetDifference(Vector.empty, Set("2", "4"), Set("3"))))
    Diff(Set(1, 2, 4), Set(1, 3)).toString shouldBe
      """|Difference at /
         |    In left but not right: 2, 4
         |    In right but not left: 3
         |
         |""".stripMargin
  }


  it should "diff lists using overriding ordered seq diff" in new DiffImplicits {
    Diff(List(1, 2, 3), List(1, 2, 3)) shouldBe Identical
    Diff(List(1, 2, 4), List(1, 3)) shouldBe Different(
      Vector(
        Difference(Vector("[1]"), "2", "3"), SetDifference(Vector("#"), Set("4"), Set.empty)
      )
    )
    Diff(List(1, 2, 4), List(1, 3)).toString shouldBe
      """|Difference at /[1]
         |    2 not equals 3
         |Difference at /#
         |    In left but not right: 4
         |
         |""".stripMargin
  }

  it should "diff lists using overriding unordered seq diff" in new UnorderedDiffImplicits {
    Diff(List(1, 2, 3), List(1, 2, 3)) shouldBe Identical
    Diff(List(1, 2, 4), List(1, 3)) shouldBe Different(Vector(SetDifference(Vector.empty, Set("2", "4"), Set("3"))))
    Diff(List(1, 2, 4), List(1, 3)).toString shouldBe
      """|Difference at /
         |    In left but not right: 2, 4
         |    In right but not left: 3
         |
         |""".stripMargin
  }

  it should "compare recursive case classes" in new DiffImplicits {
    Diff(
      Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty)))
    ) shouldBe Identical

    Diff(
      Man("John", 43, List(Baby, Woman("Lisa", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Man("John", 54, List(Woman("Sarah", 28, Nil, Set.empty)), Set(Woman("Naomi", 4, Nil, Set.empty)))
    ) should matchPattern { case Different(_) => }

    Diff(
      Man("John", 43, List(Baby, Woman("Lisa", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Man("John", 54, List(Woman("Sarah", 28, Nil, Set.empty)), Set(Woman("Naomi", 4, Nil, Set.empty)))
    ).toString shouldBe
      """|Difference at /age
         |    43 not equals 54
         |Difference at /children/[0]
         |    Baby() not equals Woman(Sarah,28,Iterable(),Iterable())
         |Difference at /children/#
         |    In left but not right: Woman(Lisa,28,Iterable(),Iterable())
         |Difference at /grandChildren
         |    In left but not right: Man(Joe,4,Iterable(),Iterable())
         |    In right but not left: Woman(Naomi,4,Iterable(),Iterable())
         |
         |""".stripMargin
  }

  it should "compare recursive coproducts" in new DiffImplicits {
    Diff[Human](
      Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty)))
    ) shouldBe Identical

    Diff(
      Man("John", 43, List(Baby, Woman("Lisa", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Woman("Alice", 60, List(Woman("Sarah", 28, Nil, Set.empty)), Set(Woman("Naomi", 4, Nil, Set.empty)))
    ) should matchPattern { case Different(_) => }

    Diff(
      Man("John", 43, List(Baby, Woman("Lisa", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Woman("Alice", 60, List(Woman("Sarah", 28, Nil, Set.empty)), Set(Woman("Naomi", 4, Nil, Set.empty)))
    ).toString shouldBe
      """|Difference at /
         |    Man(John,43,List(Baby, Woman(Lisa,28,List(),Set())),Set(Man(Joe,4,List(),Set()))) not equals Woman(Alice,60,List(Woman(Sarah,28,List(),Set())),Set(Woman(Naomi,4,List(),Set())))
         |
         |""".stripMargin
  }

  it should "compare recursive coproducts using unordered diff" in new UnorderedDiffImplicits {
    Diff[Human](
      Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Man("John", 54, List(Woman("Jess", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty)))
    ) shouldBe Identical

    Diff(
      Man("John", 43, List(Baby, Woman("Lisa", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Woman("Alice", 60, List(Woman("Sarah", 28, Nil, Set.empty)), Set(Woman("Naomi", 4, Nil, Set.empty)))
    ) should matchPattern { case Different(_) => }

    Diff(
      Man("John", 43, List(Baby, Woman("Lisa", 28, Nil, Set.empty)), Set(Man("Joe", 4, Nil, Set.empty))),
      Man("Alice", 60, List(Woman("Sarah", 28, Nil, Set.empty)), Set(Woman("Naomi", 4, Nil, Set.empty)))
    ).toString shouldBe
      """|Difference at /name
         |    John not equals Alice
         |Difference at /age
         |    43 not equals 60
         |Difference at /children
         |    In left but not right: Baby(), Woman(Lisa,28,Iterable(),Iterable())
         |    In right but not left: Woman(Sarah,28,Iterable(),Iterable())
         |Difference at /grandChildren
         |    In left but not right: Man(Joe,4,Iterable(),Iterable())
         |    In right but not left: Woman(Naomi,4,Iterable(),Iterable())
         |
         |""".stripMargin
  }
}
