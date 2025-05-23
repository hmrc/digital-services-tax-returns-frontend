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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}
import models.SimpleJson._

class BankAccountSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {
  "BankAccount" - {
    "deserialize json to Domestic bank account" in {
      val json: JsValue =
        Json.parse("""
                     |{
                     | "sortCode": "123456",
                     | "accountNo": "12345678",
                     | "_type": "uk.gov.hmrc.digitalservicestax.data.DomesticBankAccount"
                     |}""".stripMargin)

      json.as[DomesticBankAccount] mustBe DomesticBankAccount(SortCode("123456"), AccountNumber("12345678"), None)
    }

    "deserialize json to Foreign bank account" in {
      val json: JsValue =
        Json.parse("""
                     |{
                     | "iban": "FR2531682128768051490609537",
                     | "_type": "uk.gov.hmrc.digitalservicestax.data.ForeignBankAccount"
                     |}""".stripMargin)

      json.as[ForeignBankAccount] mustBe ForeignBankAccount(IBAN("FR2531682128768051490609537"))
    }

    "deserialize json for repayments" in {
      val json: JsValue =
        Json.parse("""
                     |{
                     | "accountName": "AccountName",
                     |        "bankAccount": {
                     |            "sortCode": "123456",
                     |            "accountNo": "12345678",
                     |            "_type": "uk.gov.hmrc.digitalservicestax.data.DomesticBankAccount"
                     |        }
                     |}""".stripMargin)

      json.as[RepaymentDetails] mustBe RepaymentDetails(
        AccountName("AccountName"),
        DomesticBankAccount(SortCode("123456"), AccountNumber("12345678"), None)
      )
    }

    "serialize json for repayments" in {
      val expectedJson: JsValue =
        Json.parse("""
                     |{
                     | "accountName": "AccountName",
                     |        "bankAccount": {
                     |            "sortCode": "123456",
                     |            "accountNo": "12345678",
                     |            "_type": "uk.gov.hmrc.digitalservicestax.data.DomesticBankAccount"
                     |        }
                     |}""".stripMargin)

      Json.toJson(
        RepaymentDetails(
          AccountName("AccountName"),
          DomesticBankAccount(SortCode("123456"), AccountNumber("12345678"), None)
        )
      ) mustBe expectedJson

    }

  }
}
