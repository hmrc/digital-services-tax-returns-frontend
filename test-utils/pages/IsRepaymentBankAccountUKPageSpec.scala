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

import models.{BankDetailsForRepayment, PeriodKey, UKBankDetails, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IsRepaymentBankAccountUKPageSpec extends AnyFreeSpec with Matchers {

  val periodKey: PeriodKey          = PeriodKey("001")
  val emptyUserAnswers: UserAnswers = UserAnswers("id")

  "IsRepaymentBankAccountUKPage" - {
    "cleanup" - {
      "must remove BankDetailsForRepaymentPage when there is a change of the answer from Foreign bank details' to 'UKBankDetails'" in {
        val result = emptyUserAnswers
          .set(IsRepaymentBankAccountUKPage(periodKey), true)
          .success
          .value
          .set(UKBankDetailsPage(periodKey), UKBankDetails("AccountName", "123456", "12345678", Some("1234567890")))
          .success
          .value

        result.get(BankDetailsForRepaymentPage(periodKey)) must not be defined
        result.get(UKBankDetailsPage(periodKey)) mustBe defined
      }

      "must retain IsRepaymentBankAccountUKPage value when there is a change of the answer from 'UKBankDetails' to 'Foreign bank details'" in {
        val result = emptyUserAnswers
          .set(RepaymentPage(periodKey), true)
          .success
          .value
          .set(IsRepaymentBankAccountUKPage(periodKey), false)
          .success
          .value
          .set(BankDetailsForRepaymentPage(periodKey), BankDetailsForRepayment("AccountName", "GB1234567890"))
          .success
          .value

        result.get(BankDetailsForRepaymentPage(periodKey)) mustBe defined
        result.get(UKBankDetailsPage(periodKey)) must not be defined
      }
    }
  }
}
