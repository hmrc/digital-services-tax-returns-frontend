package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.SelectActivitiesControllerPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SelectActivitiesControllerSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SelectActivitiesControllerPage).map {
      answers =>

        val value = ValueViewModel(
          HtmlContent(
            answers.map {
              answer => HtmlFormat.escape(messages(s"selectActivitiesController.$answer")).toString
            }
            .mkString(",<br>")
          )
        )

        SummaryListRowViewModel(
          key     = "selectActivitiesController.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.SelectActivitiesControllerController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("selectActivitiesController.change.hidden"))
          )
        )
    }
}
