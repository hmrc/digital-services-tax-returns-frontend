package forms

import forms.behaviours.CurrencyFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends CurrencyFieldBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validCurrencyDataGenerator
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )

    behave like currencyField(
      form,
      fieldName,
      invalidError = FormError(fieldName, "$className;format="decap"$.error.invalid"),
      exceededError = FormError(fieldName, "$className;format="decap"$.error.exceeded")
    )

  }
}
