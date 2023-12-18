package forms

import forms.behaviours.FieldBehaviours
import play.api.data.FormError

class ReportSocialMediaOperatingMarginFormProviderSpec extends FieldBehaviours {

  val group = "group"
  val form  = new ReportSocialMediaOperatingMarginFormProvider()(group)

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
      requiredError = FormError(fieldName, "reportSocialMediaOperatingMargin.error.required", Seq(group))
    )
  }
}
