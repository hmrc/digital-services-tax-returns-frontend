package forms

import forms.behaviours.OptionFieldBehaviours
import models.SocialMediaLoss
import play.api.data.FormError

class SocialMediaLossFormProviderSpec extends OptionFieldBehaviours {

  val form = new SocialMediaLossFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "socialMediaLoss.error.required"

    behave like optionsField[SocialMediaLoss](
      form,
      fieldName,
      validValues  = SocialMediaLoss.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
