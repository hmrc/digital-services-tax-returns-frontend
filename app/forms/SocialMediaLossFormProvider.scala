package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.SocialMediaLoss

class SocialMediaLossFormProvider @Inject() extends Mappings {

  def apply(): Form[SocialMediaLoss] =
    Form(
      "value" -> enumerable[SocialMediaLoss]("socialMediaLoss.error.required")
    )
}
