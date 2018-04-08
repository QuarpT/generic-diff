package com.tailrec.diff

import org.scalatest.{FlatSpec, Matchers}

class GenericDiffTest extends FlatSpec with Matchers with GenericDiff {
  case class Outer(a: Int, b: Int, c: Int, e: Inner)
  case class Inner(f: Int, g: Int)

  it should "diff case classes correctly" in {
    Diff(Outer(1,2,3,Inner(4,4)), Outer(1,2,2,Inner(4,5))).description.replaceAll("\n| ", "") shouldBe
      """
        |Difference found at c
        | 3
        | not equals
        | 2
        |
        |Difference found at e.g
        | 4
        | not equals
        | 5
      """.stripMargin.replaceAll("\n| ", "")
  }
}
