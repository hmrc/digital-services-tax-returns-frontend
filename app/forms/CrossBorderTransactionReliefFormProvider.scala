package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class CrossBorderTransactionReliefFormProvider @Inject() extends Mappings {

  def apply(): Form[Int] =
    Form(
      "value" -> int(
        "crossBorderTransactionRelief.error.required",
        "crossBorderTransactionRelief.error.wholeNumber",
        "crossBorderTransactionRelief.error.nonNumeric")
          .verifying(inRange(0, Int.MaxValue, "crossBorderTransactionRelief.error.outOfRange"))
    )
}
