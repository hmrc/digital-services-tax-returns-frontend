package forms

import forms.behaviours.FieldBehaviours
import play.api.data.FormError

class ReportSocialMediaOperatingMarginFormProviderSpec extends FieldBehaviours {

  val form = new ReportSocialMediaOperatingMarginFormProvider()()

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
      requiredError = FormError(fieldName, "reportSocialMediaOperatingMargin.error.required")
    )
  }
}
