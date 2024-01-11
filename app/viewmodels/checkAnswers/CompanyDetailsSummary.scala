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
import models.{CheckMode, Index, UserAnswers}
import pages.CompanyDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CompanyDetailsSummary {

  def row(periodKey: String, index: Index, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CompanyDetailsPage(periodKey, index)).map { answer =>
      val value = HtmlFormat.escape(answer.companyName).toString

      SummaryListRowViewModel(
        key = Key(HtmlContent(value)),
        value = ValueViewModel(Empty),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.CompanyDetailsController.onPageLoad(periodKey, index, CheckMode).url
          )
            .withVisuallyHiddenText(messages("companyDetails.change.hidden")),
          ActionItemViewModel("site.remove", routes.CompanyDetailsController.onDelete(periodKey, index, CheckMode).url)
            .withVisuallyHiddenText(messages("companyDetails.remove.hidden"))
            .withCssClass("")
        )
      ).withCssClass("dst-listing")
    }

}
