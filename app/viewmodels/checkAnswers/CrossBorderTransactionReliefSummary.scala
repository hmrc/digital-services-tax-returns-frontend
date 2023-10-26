package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.CrossBorderTransactionReliefPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CrossBorderTransactionReliefSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CrossBorderTransactionReliefPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "crossBorderTransactionRelief.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.CrossBorderTransactionReliefController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("crossBorderTransactionRelief.change.hidden"))
          )
        )
    }
}
