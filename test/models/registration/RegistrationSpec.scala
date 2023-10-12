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

import generators.ModelGenerators._
import models.BackendAndFrontendJson._
import models.{AddressLine, CompanyName, DSTRegNumber, Email, PhoneNumber, Postcode, RestrictiveString, SafeId}
import org.scalacheck.Arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

class RegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "Registration" - {

    "must serialise and de-serialise registration model" in {
      val registration = Arbitrary.arbitrary[Registration].sample.value
      val  json = Json.toJson[Registration](registration)
      json.as[Registration] mustBe registration
    }

    "de-serialise registration model" in {
      val registration = Registration(CompanyRegWrapper(Company(CompanyName("SAP Intern"),
        UkAddress(AddressLine("The house"),
          Some(AddressLine("The Road")),
          Some(AddressLine("line3")),
          Some(AddressLine("line4")),
          Postcode("HG18 3RE"))), None, Some(SafeId("XJ0008010817305")),
        false), None,
        Some(Company(CompanyName("GroupName"),
          UkAddress(AddressLine("1 Road Street"),
            Some(AddressLine("City")), Some(AddressLine("City")),
            Some(AddressLine("County")), Postcode("AB12 1AB")))),
        ContactDetails(RestrictiveString("John"), RestrictiveString("Smith"), PhoneNumber("09876543234"),
          Email("test@email.com")), LocalDate.parse("2020-04-20"), LocalDate.parse("2020-12-31"), Some(DSTRegNumber("AMDST0799721562")))

      val json: JsValue = Json.parse("""| {"companyReg" : {
                                        |            "company" : {
                                        |                "name" : "SAP Intern",
                                        |                "address" : {
                                        |                    "line1" : "The house",
                                        |                    "line2" : "The Road",
                                        |                    "line3" : "line3",
                                        |                    "line4" : "line4",
                                        |                    "postalCode" : "HG18 3RE",
                                        |                    "_type" : "uk.gov.hmrc.digitalservicestax.data.UkAddress"
                                        |                }
                                        |            },
                                        |            "safeId" : "XJ0008010817305",
                                        |            "useSafeId" : false
                                        |        },
                                        |        "ultimateParent" : {
                                        |            "name" : "GroupName",
                                        |            "address" : {
                                        |                "line1" : "1 Road Street",
                                        |                "line2" : "City",
                                        |                "line3" : "City",
                                        |                "line4" : "County",
                                        |                "postalCode" : "AB12 1AB",
                                        |                "_type" : "uk.gov.hmrc.digitalservicestax.data.UkAddress"
                                        |            }
                                        |        },
                                        |        "contact" : {
                                        |            "forename" : "John",
                                        |            "surname" : "Smith",
                                        |            "phoneNumber" : "09876543234",
                                        |            "email" : "test@email.com"
                                        |        },
                                        |        "dateLiable" : "2020-04-20",
                                        |        "accountingPeriodEnd" : "2020-12-31",
                                        |        "registrationNumber" : "AMDST0799721562"
                                        | }""".stripMargin)

      json.as[Registration] mustBe registration
    }

  }
}
