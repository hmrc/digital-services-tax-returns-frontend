package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ReportOnlineMarketplaceLossPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ReportOnlineMarketplaceLossSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ReportOnlineMarketplaceLossPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "reportOnlineMarketplaceLoss.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReportOnlineMarketplaceLossController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("reportOnlineMarketplaceLoss.change.hidden"))
          )
        )
    }
}
