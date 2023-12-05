package forms

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Double] =
    Form(
      "value" -> percentage(
        "$className;format="decap"$.error.required",
        "$className;format="decap"$.error.invalid",
        "$className;format="decap"$.error.exceeded"
      )
    )

}
