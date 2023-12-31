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

package forms.behaviours

import play.api.data.{Form, FormError}

trait CurrencyFieldBehaviours extends FieldBehaviours {

  def currencyField(form: Form[_], fieldName: String, invalidError: FormError, exceededError: FormError): Unit = {
    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") { nonNumeric =>
        val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
        result.errors must contain only invalidError
      }
    }

    "not bind invalid decimal numbers" in {
      forAll(decimalNumbersWithMaxLength -> "decimal") { decimal =>
        val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
        result.errors must contain only exceededError
      }
    }

    "not bind decimal exceeding more then 2 decimal points" in {
      forAll(numberWith3DP -> "decimal") { decimal =>
        val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
        result.errors must contain only invalidError
      }
    }
  }

}
