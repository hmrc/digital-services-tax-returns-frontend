/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, PeriodKey, UserAnswers}
import pages.UKBankDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object UKBankDetailsSummary {

  def row(periodKey: PeriodKey, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UKBankDetailsPage(periodKey)).map { answer =>
      val value = HtmlFormat.escape(answer.accountName).toString + "<br/>" + HtmlFormat.escape(answer.sortCode).toString

      SummaryListRowViewModel(
        key = "uKBankDetails.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel("site.change", routes.UKBankDetailsController.onPageLoad(periodKey, CheckMode).url)
            .withVisuallyHiddenText(messages("uKBankDetails.change.hidden"))
        )
      )
    }
}
