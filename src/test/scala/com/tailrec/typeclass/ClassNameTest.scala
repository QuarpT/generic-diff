package com.tailrec.typeclass

import org.scalatest.{FlatSpec, Matchers}
import com.tailrec.typeclass.ClassName._

class ClassNameTest extends FlatSpec with Matchers {
  it should "output the class name" in {
    ClassName[Baby.type] shouldBe "Baby"
    ClassName[Woman] shouldBe "Woman"
    ClassName[Man] shouldBe "Man"
    ClassName[Human] shouldBe "Human"
  }
}
