package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class SocialMediaLossSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "SocialMediaLoss" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(SocialMediaLoss.values.toSeq)

      forAll(gen) {
        socialMediaLoss =>

          JsString(socialMediaLoss.toString).validate[SocialMediaLoss].asOpt.value mustEqual socialMediaLoss
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!SocialMediaLoss.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[SocialMediaLoss] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(SocialMediaLoss.values.toSeq)

      forAll(gen) {
        socialMediaLoss =>

          Json.toJson(socialMediaLoss) mustEqual JsString(socialMediaLoss.toString)
      }
    }
  }
}
