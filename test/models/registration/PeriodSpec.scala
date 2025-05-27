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

import models.SimpleJson
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

class PeriodSpec extends AnyFreeSpec with Matchers with SimpleJson {
  "Period" - {
    "calculate paymentDue when endDate is day of month and length of month are equal" in {
      Period(
        LocalDate.parse("2021-10-11"),
        LocalDate.parse("2022-10-31"),
        LocalDate.parse("2023-10-20"),
        Period.Key("key")
      ).paymentDue mustBe LocalDate.parse("2023-08-01")
    }

    "calculate paymentDue when endDate is day of month and length of month are not equal" in {
      Period(
        LocalDate.parse("2021-10-11"),
        LocalDate.parse("2022-10-10"),
        LocalDate.parse("2023-10-20"),
        Period.Key("key")
      ).paymentDue mustBe LocalDate.parse("2023-07-11")
    }
  }
}
