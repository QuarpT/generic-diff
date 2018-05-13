package com.tailrec.typeclass

import org.scalatest.{FlatSpec, Matchers}

class DiffTest extends FlatSpec with Matchers {

  trait Scope {

    sealed trait Human

    case class Man(age: Int, children: Vector[Human], friends: Set[Human]) extends Human

    case class Woman(age: Int, children: Vector[Human], friends: Set[Human]) extends Human

    case object Baby extends Human

  }

  it should "diff ints falling back to default diff" in new DiffImplicits {
    Diff(5, 5) shouldBe Identical
    Diff(5, 6) shouldBe Different(Vector(Difference(Vector.empty, "5", "6")))
    Diff(5, 6).toString.replaceAll("\n| ", "") shouldBe
      """|Difference at /
         |    5 not equals 6""".stripMargin.replaceAll("\n| ", "")
  }

  it should "diff sets" in new DiffImplicits {
    Diff(Set(1, 2, 3), Set(1, 2, 3)) shouldBe Identical
    Diff(Set(1, 2, 4), Set(1, 3)) shouldBe Different(Vector(SetDifference(Vector.empty, Set("2", "4"), Set("3"))))
    Diff(Set(1, 2, 4), Set(1, 3)).toString.replaceAll("\n| ", "") shouldBe
      """|Difference at /
         |    In left but not right: 2, 4
         |    In right but not left: 3
         |""".stripMargin.replaceAll("\n| ", "")
  }


  it should "diff lists using overriding ordered seq diff" in new DiffImplicits {
    Diff(List(1, 2, 3), List(1, 2, 3)) shouldBe Identical
    Diff(List(1, 2, 4), List(1, 3)) shouldBe Different(
      Vector(
        Difference(Vector("[1]"), "2", "3"), SetDifference(Vector("#"), Set("4"), Set.empty)
      )
    )
    Diff(List(1, 2, 4), List(1, 3)).toString.replaceAll("\n| ", "") shouldBe
      """|Difference at /[1]
         |    2 not equals 3
         |Difference at /#
         |    In left but not right: 4
         |""".stripMargin.replaceAll("\n| ", "")
  }

  it should "diff lists using overriding unordered seq diff" in new UnorderedDiffImplicits {
    Diff(List(1, 2, 3), List(1, 2, 3)) shouldBe Identical
    Diff(List(1, 2, 4), List(1, 3)) shouldBe Different(Vector(SetDifference(Vector.empty, Set("2", "4"), Set("3"))))
    Diff(List(1, 2, 4), List(1, 3)).toString.replaceAll("\n| ", "") shouldBe
      """|Difference at /
         |    In left but not right: 2, 4
         |    In right but not left: 3
         |""".stripMargin.replaceAll("\n| ", "")
  }

  it should "compare recursive case classes" in new DiffImplicits with Scope {
    // TODO
  }

  it should "compare recursive coproducts" in new DiffImplicits with Scope {
    // TODO
  }

  it should "compare recursive coproducts using unordered diff" in new UnorderedDiffImplicits with Scope {
    // TODO
  }

  /*

    it should "diff case classes correctly" in new DiffImplicits {
  //    println(ClassName[Outer])
      val h: Human = Man(5)
      val w: Human = Woman
  //    println(shapeless.the[Generic[Human]].to(h))
  //    println(reify(DiffPrint(h)))
  //    println(DiffPrint(w))
      println(Diff[List[Int]](List(123), List(456)).description)
      println(Diff[Human](h, w).description)
      println(Diff[Human](Outer(1,Vector(4,5,3),Set(1,3,6),Inner(4,4, Woman)), Outer(1,Vector(2,3,2,353),Set(1,2),Inner(4,5, Man(5)))).description)
      println(Diff[Human](Outer(1,Vector(2),Set(1,2,3),Inner(4,4, Woman)), Man(5)).description)
  //    UnwrapCoproduct(x)
  //    UnwrapCoproduct(x)

  //    Diff(Outer(1,2,3,Inner(4,4)), Outer(1,2,2,Inner(4,5))).description.replaceAll("\n| ", "") shouldBe
  //      """
  //        |Difference found at c
  //        | 3
  //        | not equals
  //        | 2
  //        |
  //        |Difference found at e.g
  //        | 4
  //        | not equals
  //        | 5
  //      """.stripMargin.replaceAll("\n| ", "")
    }*/
}
