package pages

import models.SelectActivitiesController
import play.api.libs.json.JsPath

case object SelectActivitiesControllerPage extends QuestionPage[Set[SelectActivitiesController]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "selectActivitiesController"
}
