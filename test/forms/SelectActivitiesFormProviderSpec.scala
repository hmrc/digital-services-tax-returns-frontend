package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.SelectActivities
import play.api.data.FormError

class SelectActivitiesFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new SelectActivitiesControllerFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "selectActivitiesController.error.required"

    behave like checkboxField[SelectActivities](
      form,
      fieldName,
      validValues  = SelectActivities.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
