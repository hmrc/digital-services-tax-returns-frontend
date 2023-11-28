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

class UKBankDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new UKBankDetailsFormProvider()()

  ".accountName" - {

    val fieldName   = "accountName"
    val requiredKey = "uKBankDetails.error.accountName.required"
    val lengthKey   = "uKBankDetails.error.accountName.length"
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
  }

  ".sortCode" - {

    val fieldName   = "sortCode"
    val requiredKey = "uKBankDetails.error.sortCode.required"
    val lengthKey   = "uKBankDetails.error.sortCode.invalid"
    val maxLength   = 8
    val regex       = """^[0-9]{6}$"""

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(regex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".accountNumber" - {

    val fieldName   = "accountNumber"
    val requiredKey = "uKBankDetails.error.accountNumber.required"
    val lengthKey   = "uKBankDetails.error.accountNumber.invalid"
    val maxLength   = 8
    val regex       = """^[0-9]{8}$"""

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(regex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".buildingNumber" - {

    val fieldName  = "buildingNumber"
    val lengthKey  = "uKBankDetails.error.buildingNumber.length"
    val invalidKey = "uKBankDetails.error.buildingNumber.invalid"
    val maxLength  = 18
    val regex      = """^[A-Za-z0-9 -]{1,18}$"""

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regex).filter(_.nonEmpty)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithInValidData(
      form,
      fieldName,
      RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{18}"),
      FormError(fieldName, invalidKey, Seq(regex))
    )
  }
}
