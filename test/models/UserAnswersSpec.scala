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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.{OptionValues, TryValues}
import pages.QuestionPage
import play.api.libs.json.JsPath

class UserAnswersSpec extends AnyFreeSpec with TryValues with OptionValues {

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
  }
}
