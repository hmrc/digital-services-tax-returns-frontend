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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class BankDetailsForRepaymentFormProviderSpec extends StringFieldBehaviours {

  val form = new BankDetailsForRepaymentFormProvider()()

  ".accountName" - {

    val fieldName   = "accountName"
    val requiredKey = "bankDetailsForRepayment.error.accountName.required"
    val lengthKey   = "bankDetailsForRepayment.error.accountName.length"
    val invalid     = "bankDetailsForRepayment.error.accountName.invalid"
    val maxLength   = 35
    val regex       = """^[a-zA-Z&^]{1,35}$"""

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from("1111111"),
      FormError(fieldName, invalid, Seq(regex))
    )
  }

  ".internationalBankAccountNumber" - {

    val fieldName   = "internationalBankAccountNumber"
    val requiredKey = "bankDetailsForRepayment.error.internationalBankAccountNumber.required"
    val lengthKey   = "bankDetailsForRepayment.error.internationalBankAccountNumber.length"
    val invalidKey  = "bankDetailsForRepayment.error.internationalBankAccountNumber.invalid"
    val iban        = "GB36BARC20051773152391"
    val maxLength   = 34

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(iban)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from("@@@@£!@£@££@£@"),
      FormError(fieldName, invalidKey, Seq())
    )

  }
}
