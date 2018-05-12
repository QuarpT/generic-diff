package com.tailrec.typeclass
import scala.reflect.runtime.universe.reify
import org.scalatest.{FlatSpec, Matchers}
import shapeless._
import ClassName._
sealed trait Human
case class Man(age: Int) extends Human
case object Woman extends Human
case class Outer(a: Int, b: Vector[Int], c: Set[Int], e: Inner) extends Human
case class Inner(f: Int, g: Int, s: Human) extends Human
//import UnorderedDiffImplicits._
import DiffImplicits._

class GenericDiffTest extends FlatSpec with Matchers {




  it should "diff case classes correctly" in {
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
  }
}
