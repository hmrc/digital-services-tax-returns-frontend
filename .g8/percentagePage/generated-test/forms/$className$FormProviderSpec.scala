package forms

import forms.behaviours.FieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends FieldBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validPercentageDataGenerator
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }
}
