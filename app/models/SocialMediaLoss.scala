package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait SocialMediaLoss

object SocialMediaLoss extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with SocialMediaLoss
  case object No extends WithName("no") with SocialMediaLoss

  val values: Seq[SocialMediaLoss] = Seq(
    Yes, No
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"socialMediaLoss.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[SocialMediaLoss] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
