package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.set
import models.SelectActivitiesController

class SelectActivitiesControllerFormProvider @Inject() extends Mappings {

  def apply(): Form[Set[SelectActivitiesController]] =
    Form(
      "value" -> set(enumerable[SelectActivitiesController]("selectActivitiesController.error.required")).verifying(nonEmptySet("selectActivitiesController.error.required"))
    )
}
