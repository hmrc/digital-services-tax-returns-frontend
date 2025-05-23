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

class RepaymentPageSpec extends AnyFreeSpec with Matchers {

  val periodKey: PeriodKey          = PeriodKey("001")
  val emptyUserAnswers: UserAnswers = UserAnswers("id")

  "RepaymentPage" - {
    "cleanup" - {
      "must remove IsRepaymentBankAccountUKPage when there is a change of the answer from 'Yes' to 'No'" in {
        val result = emptyUserAnswers
          .set(RepaymentPage(periodKey), false)
          .success
          .value

        result.get(IsRepaymentBankAccountUKPage(periodKey)) must not be defined
      }

      "must retain IsRepaymentBankAccountUKPage value when there is a change of the answer from 'No' to 'Yes'" in {
        val result = emptyUserAnswers
          .set(RepaymentPage(periodKey), true)
          .success
          .value
          .set(IsRepaymentBankAccountUKPage(periodKey), false)
          .success
          .value

        result.get(IsRepaymentBankAccountUKPage(periodKey)) mustBe defined
      }
    }
  }
}
