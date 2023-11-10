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

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class AllowanceDeductedFormProviderSpec extends CurrencyFieldBehaviours {

  val form = new AllowanceDeductedFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from("^[0-9]{1,7}(\\.[0-9]{1,2})?$")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "allowanceDeducted.error.required")
    )

    behave like currencyField(
      form,
      fieldName,
      invalidError = FormError(fieldName, "allowanceDeducted.error.invalid"),
      exceededError = FormError(fieldName, "allowanceDeducted.error.max-money")
    )

    "fail when input exceeds £25m" in {
      val result = form.bind(Map(fieldName -> "25000009")).apply(fieldName)
      result.errors.head mustEqual FormError(fieldName, "allowanceDeducted.error.max-money")
    }
  }
}
