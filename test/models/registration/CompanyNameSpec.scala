/*
 * Copyright 2025 HM Revenue & Customs
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

package models.registration

import models.{CompanyName, SimpleJson}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsNumber, JsResultException, JsString}

class CompanyNameSpec extends AnyFreeSpec with Matchers with SimpleJson {

  "CompanyName" - {

    "return CompanyName for the valid inout" in {
      JsString("company name").as[CompanyName] mustBe "company name"
    }

    "expect exception for an empty string input" in {

      val exp: JsResultException = intercept[JsResultException] {
        JsString("^^^^^!!!!***").as[CompanyName]
      }
      exp.errors.head._2.head.message mustBe "Expected a valid company name, got ^^^^^!!!!*** instead"
    }

    "expect exception for an invalid input" in {
      val exp: JsResultException = intercept[JsResultException] {
        JsNumber(12).as[CompanyName]
      }
      exp.errors.head._2.head.message mustBe "Expected a valid company name, got 12 instead"
    }

  }

}
