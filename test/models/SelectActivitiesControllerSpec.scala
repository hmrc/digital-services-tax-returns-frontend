package models

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class SelectActivitiesControllerSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "SelectActivitiesController" - {

    "must deserialise valid values" in {

      val gen = arbitrary[SelectActivitiesController]

      forAll(gen) {
        selectActivitiesController =>

          JsString(selectActivitiesController.toString).validate[SelectActivitiesController].asOpt.value mustEqual selectActivitiesController
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!SelectActivitiesController.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[SelectActivitiesController] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[SelectActivitiesController]

      forAll(gen) {
        selectActivitiesController =>

          Json.toJson(selectActivitiesController) mustEqual JsString(selectActivitiesController.toString)
      }
    }
  }
}
