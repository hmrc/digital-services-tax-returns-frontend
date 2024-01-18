/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import cats.implicits._
import cats.kernel.Monoid
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.anyvals.PosInt
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.ConfiguredPropertyChecks

class ValidatedTypeSpec extends AnyFlatSpec with Matchers with ConfiguredPropertyChecks {
  val ibanList = List(
    "AD9179714843548170724658",
    "AE532299249995935421750",
    "AL36442788709271283994894168",
    "AT836670643070032585"
  )

  implicit val arbMoney: Arbitrary[Money] = Arbitrary(
    Gen.choose(0L, 1000000000000L).map(b => Money(BigDecimal(b).setScale(2)))
  )

  it should "validate IBAN numbers from a series of concrete examples" in {
    forAll(Gen.oneOf(ibanList), minSuccessful(PosInt(86))) { source: String =>
      IBAN.of(source) shouldBe defined
    }
  }

  it should "store percentages as floats and initialise percent monoids with float monoids" in {
    Monoid[Percent].empty shouldEqual Monoid[Float].empty
  }

  it should "add up percentages using monoidal syntax" in {

    val generator = for {
      p1 <- Gen.chooseNum[Float](0f, 100f)
      p2  = 100f - p1
    } yield p1 -> p2

    forAll(generator) { case (p1, p2) =>
      whenever(p1 >= 0f && p2 >= 0f && BigDecimal(p1.toString).scale <= 3 && BigDecimal(p2.toString).scale <= 3) {
        val addedPercent = Monoid.combineAll(Seq(Percent(p1), Percent(p2)))
        val addedBytes   = Monoid.combineAll(Seq(p1, p2))
        addedPercent shouldEqual Percent(addedBytes)
      }
    }
  }

  it should "fail to construct a type with a scale of more than 3" in {
    an[IllegalArgumentException] should be thrownBy Percent.apply(1.1234f)
  }

  it should "combine and empty Money correctly" in {
    import Money.mon
    forAll { (a: Money, b: Money) =>
      a.combine(b) - a     shouldEqual b
      a.combine(b) - b     shouldEqual a
      a.combine(b) - b - a shouldEqual mon.empty
    }
  }

}
