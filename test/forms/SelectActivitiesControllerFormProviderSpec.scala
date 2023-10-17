package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.SelectActivitiesController
import play.api.data.FormError

class SelectActivitiesControllerFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new SelectActivitiesControllerFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "selectActivitiesController.error.required"

    behave like checkboxField[SelectActivitiesController](
      form,
      fieldName,
      validValues  = SelectActivitiesController.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
