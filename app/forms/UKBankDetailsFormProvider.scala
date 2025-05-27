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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.UKBankDetails

class UKBankDetailsFormProvider @Inject() extends Mappings {

  val accountNameRegex        = """^[a-zA-Z&^]{1,35}$"""
  val sortCodeRegex           = """^[0-9]{6}$"""
  val accountNumberRegex      = """^[0-9]{8}$"""
  val buildingRollNumberRegex = """^[A-Za-z0-9 -]{1,18}$"""

  def apply(): Form[UKBankDetails] = Form(
    mapping(
      "accountName"    -> text("uKBankDetails.error.accountName.required")
        .verifying(
          firstError(
            maxLength(35, "uKBankDetails.error.accountName.length"),
            regexp(accountNameRegex, "uKBankDetails.error.accountName.invalid")
          )
        ),
      "sortCode"       -> text("uKBankDetails.error.sortCode.required")
        .verifying(regexp(sortCodeRegex, "uKBankDetails.error.sortCode.invalid")),
      "accountNumber"  -> text("uKBankDetails.error.accountNumber.required")
        .verifying(regexp(accountNumberRegex, "uKBankDetails.error.accountNumber.invalid")),
      "buildingNumber" -> optional(
        text()
          .verifying(
            firstError(
              maxLength(18, "uKBankDetails.error.buildingNumber.length"),
              regexp(buildingRollNumberRegex, "uKBankDetails.error.buildingNumber.invalid")
            )
          )
      )
    )(UKBankDetails.apply)(UKBankDetails.unapply)
  )
}
