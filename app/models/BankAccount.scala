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

package models

import play.api.libs.json.{Json, JsonConfiguration, JsonNaming, OFormat}

sealed trait BankAccount

final case class ForeignBankAccount(iban: IBAN) extends BankAccount

final case class DomesticBankAccount(
  sortCode: SortCode,
  accountNo: AccountNumber,
  buildingSocietyNumber: Option[BuildingSocietyRollNumber]
) extends BankAccount

final case class RepaymentDetails(
  accountName: AccountName,
  bankAccount: BankAccount
)

object DomesticBankAccount extends SimpleJson {
  implicit val domesticBankAccountFormat: OFormat[DomesticBankAccount] = Json.format[DomesticBankAccount]
}

object ForeignBankAccount extends SimpleJson {
  implicit val foreignBankAccountFormat: OFormat[ForeignBankAccount] = Json.format[ForeignBankAccount]
}

object BankAccount {

  private[models] val jsonConfig            = JsonConfiguration(
    discriminator = "_type",
    typeNaming = JsonNaming { fullName =>
      s"uk.gov.hmrc.digitalservicestax.data.${fullName.split("\\.").last}"
    }
  )
  implicit val format: OFormat[BankAccount] = Json.configured(jsonConfig).format[BankAccount]

}
