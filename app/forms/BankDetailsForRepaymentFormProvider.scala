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

package forms

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.BankDetailsForRepayment

class BankDetailsForRepaymentFormProvider @Inject() extends Mappings {

  val accountNameRegex                    = """^[a-zA-Z&^]{1,35}$"""
  private val accountNameLengthErrorKey   = "bankDetailsForRepayment.error.accountName.length"
  private val accountNameMaxLength        = 35
  private val accountNameRequiredErrorKey = "bankDetailsForRepayment.error.accountName.required"
  private val accountNameInvalidErrorKey  = "bankDetailsForRepayment.error.accountName.invalid"
  private val ibanErrorKey                = "bankDetailsForRepayment.error.internationalBankAccountNumber.required"
  private val ibanLength                  = 34
  private val ibanLengthErrorKey          = "bankDetailsForRepayment.error.internationalBankAccountNumber.length"
  private val ibanInvalidErrorKey         = "bankDetailsForRepayment.error.internationalBankAccountNumber.invalid"

  def apply(): Form[BankDetailsForRepayment] = Form(
    mapping(
      "accountName"                    -> text(accountNameRequiredErrorKey)
        .verifying(
          firstError(
            maxLength(accountNameMaxLength, accountNameLengthErrorKey),
            regexp(accountNameRegex, accountNameInvalidErrorKey)
          )
        ),
      "internationalBankAccountNumber" -> text(ibanErrorKey)
        .verifying(firstError(maxLength(ibanLength, ibanLengthErrorKey), ibanCheck(ibanInvalidErrorKey)))
    )(BankDetailsForRepayment.apply)(BankDetailsForRepayment.unapply)
  )
}
