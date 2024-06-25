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
      userAnswers.set(SelectActivitiesPage(periodKey),SelectActivities.values.toSet)

      Activity.convertToActivity(SelectActivities.values.toSet) mustBe Set(SocialMedia, SearchEngine, OnlineMarketplace)
      Activity.convertToActivity(Set.empty) mustBe Set.empty
    }
  }
}
