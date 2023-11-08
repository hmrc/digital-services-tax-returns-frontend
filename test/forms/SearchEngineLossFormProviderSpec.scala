package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class SearchEngineLossFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "searchEngineLoss.error.required"
  val invalidKey = "error.boolean"

  val form = new SearchEngineLossFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
