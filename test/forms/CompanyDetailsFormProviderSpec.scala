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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class CompanyDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new CompanyDetailsFormProvider()()

  ".companyName" - {

    val fieldName        = "companyName"
    val requiredKey      = "companyDetails.error.companyName.required"
    val lengthKey        = "companyDetails.error.companyName.length"
    val invalidKey       = "companyDetails.error.companyName.invalid"
    val companyNameRegex = """^[a-zA-Z0-9 '&.-]{1,105}$"""
    val maxLength        = 105

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(companyNameRegex).filter(_.trim.nonEmpty)
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
      RegexpGen.from(s"[!£^*(){}_+=:;|`~,±üçñèé@]{105}"),
      invalidDataError = FormError(fieldName, invalidKey, Seq(companyNameRegex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".uniqueTaxpayerReference" - {

    val fieldName                  = "uniqueTaxpayerReference"
    val invalidKey                 = "companyDetails.error.uniqueTaxpayerReference.invalid"
    val uniqueTaxReferenceMaxRegex = "^[0-9]{10}$"
    val maxLength                  = 10

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(uniqueTaxReferenceMaxRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, invalidKey, Seq(uniqueTaxReferenceMaxRegex))
    )

  }
}
