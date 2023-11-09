package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends CurrencyFieldBehaviours {

  val form = new $className$FormProvider()()
  val currencyRegex = "^\\d{1,15}(\\.\\d{2})?$"

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(currencyRegex)
    )

    behave like currencyField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "$className;format="decap"$.error.invalid"),
      wholeNumberError = FormError(fieldName, "$className;format="decap"$.error.exceeded")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }
}
