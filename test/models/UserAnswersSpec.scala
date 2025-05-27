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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.{OptionValues, TryValues}
import pages.QuestionPage
import play.api.libs.json.{JsArray, JsBoolean, JsPath, JsString, Json}

class UserAnswersSpec extends AnyFreeSpec with TryValues with OptionValues {

  private val data = Json.obj(
    "004" -> Json.obj(
      "company-details"         -> JsArray(
        Seq(
          Json.toJson(CompanyDetails("fun ltd", Some("1234567890"))),
          Json.toJson(CompanyDetails("boring ltd", Some("1234867491")))
        )
      ),
      "reportAlternativeCharge" -> JsBoolean(false),
      "reportCrossBorderRelief" -> JsBoolean(false),
      "selectActivities"        -> JsArray(Seq(JsString("Social media"), JsString("Online Marketplace")))
    )
  )

  case class SamplePage() extends QuestionPage[String] {
    override def path: JsPath = JsPath \ toString

    override def toString: String = "sample-page"
  }

  "UserAnswers" - {
    ".set" - {
      "must update the userAnswers" in {
        val userAnswers = UserAnswers("id").set(SamplePage(), "value1").success.value
        userAnswers.get(SamplePage()).value mustBe "value1"
        SamplePage().toString mustBe "sample-page"
      }
    }

    ".remove" - {
      "must remove the data from userAnswers" in {
        val userAnswers        = UserAnswers("id").set(SamplePage(), "value1").success.value
        val updatedUserAnswers = userAnswers.remove(SamplePage()).success.value

        updatedUserAnswers.get(SamplePage()) mustBe None
      }
    }

    ".getByPeriodKey" - {
      "must get all data associated with a period key" in {
        val userAnswers = UserAnswers("Int-123-456-789", data)

        val dataByPeriodKey = userAnswers.getByPeriodKey(PeriodKey("004"))

        dataByPeriodKey.size mustEqual 4
        dataByPeriodKey("company-details") mustEqual JsArray(
          Seq(
            Json.toJson(CompanyDetails("fun ltd", Some("1234567890"))),
            Json.toJson(CompanyDetails("boring ltd", Some("1234867491")))
          )
        )
        dataByPeriodKey("reportAlternativeCharge") mustEqual JsBoolean(false)
        dataByPeriodKey("reportCrossBorderRelief") mustEqual JsBoolean(false)
        dataByPeriodKey("selectActivities") mustEqual JsArray(
          Seq(JsString("Social media"), JsString("Online Marketplace"))
        )
      }
    }

    ".findByAttr" - {
      val userAnswers = UserAnswers("Int-123-456-789", data)

      userAnswers.findByAttr[List[CompanyDetails]](PeriodKey("004"), "company-details").value mustEqual List(
        CompanyDetails("fun ltd", Some("1234567890")),
        CompanyDetails("boring ltd", Some("1234867491"))
      )
      userAnswers.findByAttr[Boolean](PeriodKey("004"), "reportAlternativeCharge").value mustEqual false
      userAnswers.findByAttr[Boolean](PeriodKey("004"), "reportCrossBorderRelief").value mustEqual false
      userAnswers.findByAttr[Seq[String]](PeriodKey("004"), "selectActivities").value mustEqual Seq(
        "Social media",
        "Online Marketplace"
      )
    }
  }
}
