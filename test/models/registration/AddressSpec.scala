/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{AddressLine, CountryCode, Postcode}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

class AddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "serialize and de-serialise uk address model" in {
    val json: JsValue = Json.parse("""|  {
         |    "line1" : "The house",
         |    "line2" : "The Road",
         |     "line3" : "line3",
         |     "line4" : "line4",
         |     "postalCode" : "HG18 3RE",
         |      "_type" : "uk.gov.hmrc.digitalservicestax.data.UkAddress"
         | }""".stripMargin)

    val address = json.as[Address]
    address.countryCode mustBe CountryCode("GB")
    address mustBe UkAddress(
      AddressLine("The house"),
      Some(AddressLine("The Road")),
      Some(AddressLine("line3")),
      Some(AddressLine("line4")),
      Postcode("HG18 3RE")
    )

  }

  "serialize and de-serialise non uk address model" in {
    val json: JsValue = Json.parse("""|  {
         |    "line1" : "The house",
         |    "line2" : "The Road",
         |     "line3" : "line3",
         |     "line4" : "line4",
         |     "countryCode" : "FR",
         |      "_type" : "uk.gov.hmrc.digitalservicestax.data.ForeignAddress"
         | }""".stripMargin)

    val address = json.as[Address]
    address.postalCode mustBe ""
    address mustBe ForeignAddress(
      AddressLine("The house"),
      Some(AddressLine("The Road")),
      Some(AddressLine("line3")),
      Some(AddressLine("line4")),
      CountryCode("FR")
    )
  }
}
