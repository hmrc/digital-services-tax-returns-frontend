package pages

import play.api.libs.json.JsPath

case object ReportSocialMediaOperatingMarginPage extends QuestionPage[Double] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "reportSocialMediaOperatingMargin"
}
