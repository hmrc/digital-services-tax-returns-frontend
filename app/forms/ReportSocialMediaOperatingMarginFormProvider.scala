package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ReportSocialMediaOperatingMarginFormProvider @Inject() extends Mappings {

  def apply(): Form[Double] =
    Form(
      "value" -> percentage(
        "reportSocialMediaOperatingMargin.error.required",
        "reportSocialMediaOperatingMargin.error.invalid"
      )
    )

}
