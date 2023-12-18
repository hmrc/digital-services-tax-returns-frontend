package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ReportSocialMediaOperatingMarginPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ReportSocialMediaOperatingMarginSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ReportSocialMediaOperatingMarginPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "reportSocialMediaOperatingMargin.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReportSocialMediaOperatingMarginController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("reportSocialMediaOperatingMargin.change.hidden"))
          )
        )
    }
}
