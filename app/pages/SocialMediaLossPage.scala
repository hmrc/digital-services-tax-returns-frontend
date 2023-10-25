package pages

import models.SocialMediaLoss
import play.api.libs.json.JsPath

case object SocialMediaLossPage extends QuestionPage[SocialMediaLoss] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "socialMediaLoss"
}
