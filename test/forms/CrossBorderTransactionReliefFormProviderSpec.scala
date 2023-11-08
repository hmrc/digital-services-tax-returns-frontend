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

class CrossBorderTransactionReliefFormProviderSpec extends CurrencyFieldBehaviours {

  val form          = new CrossBorderTransactionReliefFormProvider()()
  val currencyRegex = "^\\d{1,15}(\\.\\d{2})?$"

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(currencyRegex)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "crossBorderTransactionRelief.error.required")
    )

    behave like currencyField(
      form,
      fieldName,
      FormError(fieldName, "crossBorderTransactionRelief.error.invalid")
    )
  }
}