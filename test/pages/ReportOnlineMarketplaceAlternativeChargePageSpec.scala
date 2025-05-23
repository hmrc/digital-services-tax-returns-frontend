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

package pages

import models.{PeriodKey, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ReportOnlineMarketplaceAlternativeChargePageSpec extends AnyFreeSpec with Matchers {
  val periodKey: PeriodKey          = PeriodKey("001")
  val emptyUserAnswers: UserAnswers = UserAnswers("id")
  val data: Double                  = 122.34

  "ReportOnlineMarketplaceAlternativeChargePage" - {
    "cleanup" - {
      "must remove ReportOnlineMarketplaceLossPage, ReportOnlineMarketplaceOperatingMarginPage when there is a change of the answer from 'Yes' to 'No'" in {
        val result = emptyUserAnswers
          .set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), data)
          .success
          .value
          .set(ReportOnlineMarketplaceLossPage(periodKey), false)
          .success
          .value
          .set(ReportOnlineMarketplaceAlternativeChargePage(periodKey), false)
          .success
          .value

        result.get(ReportOnlineMarketplaceLossPage(periodKey))            must not be defined
        result.get(ReportOnlineMarketplaceOperatingMarginPage(periodKey)) must not be defined
      }

      "must retain ReportOnlineMarketplaceOperatingMarginPage, ReportOnlineMarketplaceLossPage when there is a change of the answer to 'Yes'" in {

        val result = emptyUserAnswers
          .set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), data)
          .success
          .value
          .set(ReportOnlineMarketplaceLossPage(periodKey), false)
          .success
          .value
          .set(ReportOnlineMarketplaceAlternativeChargePage(periodKey), true)
          .success
          .value

        result.get(ReportOnlineMarketplaceLossPage(periodKey)) mustBe defined
        result.get(ReportOnlineMarketplaceOperatingMarginPage(periodKey)) mustBe defined
      }
    }
  }
}
