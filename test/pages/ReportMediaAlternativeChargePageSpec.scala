/*
 * Copyright 2024 HM Revenue & Customs
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

package pages

import models.{PeriodKey, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ReportMediaAlternativeChargePageSpec extends AnyFreeSpec with Matchers {
  val periodKey: PeriodKey          = PeriodKey("001")
  val emptyUserAnswers: UserAnswers = UserAnswers("id")
  val data: Double                  = 122.34

  "ReportMediaAlternativeChargePage" - {
    "cleanup" - {

      "must remove SocialMediaLossPage, ReportSocialMediaOperatingMarginPage when there is a change of the answer from 'Yes' to 'No'" in {
        val result = emptyUserAnswers
          .set(ReportSocialMediaOperatingMarginPage(periodKey), data)
          .success
          .value
          .set(SocialMediaLossPage(periodKey), false)
          .success
          .value
          .set(ReportMediaAlternativeChargePage(periodKey), false)
          .success
          .value

        result.get(SocialMediaLossPage(periodKey))                  must not be defined
        result.get(ReportSocialMediaOperatingMarginPage(periodKey)) must not be defined
      }

      "must retain ReportSocialMediaOperatingMarginPage, SocialMediaLossPage when there is a change of the answer to 'Yes'" in {

        val result = emptyUserAnswers
          .set(ReportSocialMediaOperatingMarginPage(periodKey), data)
          .success
          .value
          .set(SocialMediaLossPage(periodKey), false)
          .success
          .value
          .set(ReportMediaAlternativeChargePage(periodKey), true)
          .success
          .value

        result.get(SocialMediaLossPage(periodKey)) mustBe defined
        result.get(ReportSocialMediaOperatingMarginPage(periodKey)) mustBe defined
      }
    }
  }
}
