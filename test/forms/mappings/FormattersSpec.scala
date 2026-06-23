/*
 * Copyright 2026 HM Revenue & Customs
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

package forms.mappings

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import models.Enumerable

enum TestEnum {
  case OptionA, OptionB
}

object TestEnum {
  given Enumerable[TestEnum] with {
    def withName(str: String): Option[TestEnum] = str match {
      case "OptionA" => Some(TestEnum.OptionA)
      case "OptionB" => Some(TestEnum.OptionB)
      case _         => None
    }
  }
}

class FormattersSpec extends AnyWordSpec with Matchers with Formatters {

  val testKey = "value"
  val errorKey = "error.required"
  val args: Seq[String] = Seq("arg1")

  "stringFormatter" should {
    val formatter = stringFormatter(errorKey, args)

    "bind valid string data and trim it" in {
      formatter.bind(testKey, Map(testKey -> "  hello  ")) shouldBe Right("hello")
    }

    "fail to bind when key is missing" in {
      formatter.bind(testKey, Map.empty) shouldBe Left(Seq(FormError(testKey, errorKey, args)))
    }

    "fail to bind when string is empty after trimming" in {
      formatter.bind(testKey, Map(testKey -> "   ")) shouldBe Left(Seq(FormError(testKey, errorKey, args)))
    }

    "unbind a string value" in {
      formatter.unbind(testKey, "hello") shouldBe Map(testKey -> "hello")
    }
  }

  "booleanFormatter" should {
    val invalidKey = "error.invalid"
    val formatter = booleanFormatter(errorKey, invalidKey, args)

    "bind 'true' to true" in {
      formatter.bind(testKey, Map(testKey -> "true")) shouldBe Right(true)
    }

    "bind 'false' to false" in {
      formatter.bind(testKey, Map(testKey -> "false")) shouldBe Right(false)
    }

    "fail to bind on invalid boolean string" in {
      formatter.bind(testKey, Map(testKey -> "notABool")) shouldBe Left(Seq(FormError(testKey, invalidKey, args)))
    }

    "fail to bind on missing key" in {
      formatter.bind(testKey, Map.empty) shouldBe Left(Seq(FormError(testKey, errorKey, args)))
    }

    "unbind a boolean value" in {
      formatter.unbind(testKey, true) shouldBe Map(testKey -> "true")
    }
  }

  "intFormatter" should {
    val wholeNumberKey = "error.whole"
    val nonNumericKey = "error.nonNumeric"
    val formatter = intFormatter(errorKey, wholeNumberKey, nonNumericKey, args)

    "bind a valid integer string" in {
      formatter.bind(testKey, Map(testKey -> "1,234")) shouldBe Right(1234)
    }

    "fail to bind on a decimal string" in {
      formatter.bind(testKey, Map(testKey -> "12.34")) shouldBe Left(Seq(FormError(testKey, wholeNumberKey, args)))
    }

    "fail to bind on non-numeric strings" in {
      formatter.bind(testKey, Map(testKey -> "abc")) shouldBe Left(Seq(FormError(testKey, nonNumericKey, args)))
    }

    "unbind an integer value" in {
      formatter.unbind(testKey, 42) shouldBe Map(testKey -> "42")
    }
  }

  "percentageFormatter" should {
    val invalidKey = "error.invalid"
    val formatter = percentageFormatter(errorKey, invalidKey, args)

    "bind valid percentages" in {
      formatter.bind(testKey, Map(testKey -> "50.55")) shouldBe Right(50.55)
      formatter.bind(testKey, Map(testKey -> "0")) shouldBe Right(0.0)
      formatter.bind(testKey, Map(testKey -> "100")) shouldBe Right(100.0)
    }

    "fail to bind values out of 0-100 range" in {
      formatter.bind(testKey, Map(testKey -> "100.1")) shouldBe Left(Seq(FormError(testKey, invalidKey, args)))
      formatter.bind(testKey, Map(testKey -> "-1")) shouldBe Left(Seq(FormError(testKey, invalidKey, args)))
    }

    "fail to bind values with more than 3 decimal places" in {
      formatter.bind(testKey, Map(testKey -> "50.1234")) shouldBe Left(Seq(FormError(testKey, invalidKey, args)))
    }

    "unbind percentage values correctly removing trailing .0" in {
      formatter.unbind(testKey, 50.0) shouldBe Map(testKey -> "50")
      formatter.unbind(testKey, 50.5) shouldBe Map(testKey -> "50.5")
    }
  }

  "currencyFormatter" should {
    val invalidKey = "error.invalid"
    val exceededKey = "error.exceeded"
    val maxMoneyKey = "error.maxMoney"

    val formatter = currencyFormatter(errorKey, invalidKey, exceededKey, Some(maxMoneyKey), args)

    "bind valid currency amounts" in {
      formatter.bind(testKey, Map(testKey -> "12,345.67")) shouldBe Right(BigDecimal("12345.67"))
    }

    "fail to bind when length exceeds max money precision limit" in {
      val massiveNum = "1" * 20
      formatter.bind(testKey, Map(testKey -> massiveNum)) shouldBe Left(Seq(FormError(testKey, exceededKey, args)))
    }

    "fail to bind when value exceeds max money limit" in {
      formatter.bind(testKey, Map(testKey -> "25000001")) shouldBe Left(Seq(FormError(testKey, maxMoneyKey, args)))
    }

    "fail to bind invalid formats" in {
      formatter.bind(testKey, Map(testKey -> "12.345")) shouldBe Left(Seq(FormError(testKey, invalidKey, args)))
    }

    "unbind currency values formatted to 2 decimal places" in {
      formatter.unbind(testKey, BigDecimal("12.3")) shouldBe Map(testKey -> "12.30")
    }
  }

  "enumerableFormatter" should {
    val invalidKey = "error.invalid"
    import TestEnum.given
    val formatter = enumerableFormatter[TestEnum](errorKey, invalidKey, args)

    "bind valid enum values" in {
      formatter.bind(testKey, Map(testKey -> "OptionA")) shouldBe Right(TestEnum.OptionA)
    }

    "fail to bind invalid enum values" in {
      formatter.bind(testKey, Map(testKey -> "OptionC")) shouldBe Left(Seq(FormError(testKey, invalidKey, args)))
    }

    "unbind enum values" in {
      formatter.unbind(testKey, TestEnum.OptionB) shouldBe Map(testKey -> "OptionB")
    }
  }
}
