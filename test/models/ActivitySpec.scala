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

import models.Activity.{OnlineMarketplace, SearchEngine, SocialMedia}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.SelectActivitiesPage

class ActivitySpec extends AnyFreeSpec {

  private val periodKey = PeriodKey("003")

  "Activity" - {
    "convert user answers to activity" in {
      val userAnswers = UserAnswers("id")
      userAnswers.set(SelectActivitiesPage(periodKey), SelectActivities.values.toSet)

      Activity.convertToActivity(SelectActivities.values.toSet) mustBe Set(SocialMedia, SearchEngine, OnlineMarketplace)
      Activity.convertToActivity(Set.empty) mustBe Set.empty
    }
  }
}
