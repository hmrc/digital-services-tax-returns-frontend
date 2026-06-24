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

package models.requests

import controllers.convertOptionToValuable
import generators.ModelGenerators.*
import models.registration.{Period, Registration}
import models.requests.IdentifierRequest
import org.scalacheck.Arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.test.FakeRequest

import java.time.LocalDate

class IdentifierRequestSpec extends AnyFreeSpec with Matchers {

  "submittedPeriodStart" - {

    "must return the formatted start date when period exists" in {

      val period = Period(
        start = LocalDate.of(2024, 1, 1),
        end = LocalDate.of(2024, 3, 31),
        returnDue = LocalDate.of(2024, 4, 30),
        key = Period.Key("24A1")
      )

      val request = IdentifierRequest(
        request = FakeRequest(),
        userId = "userId",
        registration = Arbitrary.arbitrary[Registration].sample.value,
        period = Some(period)
      )

      request.submittedPeriodStart must not be "Failed to get the start date"
    }

    "must return the fallback message when period is missing" in {

      val request = IdentifierRequest(
        request = FakeRequest(),
        userId = "userId",
        registration = Arbitrary.arbitrary[Registration].sample.value,
        period = None
      )

      request.submittedPeriodStart mustBe "Failed to get the start date"
    }
  }

  "submittedPeriodEnd" - {

    "must return the formatted end date when period exists" in {

      val period = Period(
        start = LocalDate.of(2024, 1, 1),
        end = LocalDate.of(2024, 3, 31),
        returnDue = LocalDate.of(2024, 4, 30),
        key = Period.Key("24A1")
      )

      val request = IdentifierRequest(
        request = FakeRequest(),
        userId = "userId",
        registration = Arbitrary.arbitrary[Registration].sample.value,
        period = Some(period)
      )

      request.submittedPeriodEnd must not be "Failed to get the end date"
    }

    "must return the fallback message when period is missing" in {

      val request = IdentifierRequest(
        request = FakeRequest(),
        userId = "userId",
        registration = Arbitrary.arbitrary[Registration].sample.value,
        period = None
      )

      request.submittedPeriodEnd mustBe "Failed to get the end date"
    }
  }
}
