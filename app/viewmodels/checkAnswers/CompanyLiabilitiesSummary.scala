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
import pages.CompanyLiabilitiesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CompanyLiabilitiesSummary {

  def row(answers: UserAnswers, periodKey: String, index: Index)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CompanyLiabilitiesPage(periodKey, index)).map { answer =>
      SummaryListRowViewModel(
        key = "companyLiabilities.checkYourAnswersLabel",
        value = ValueViewModel(answer.toString),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.CompanyLiabilitiesController.onPageLoad(periodKey, CheckMode, index).url
          )
            .withVisuallyHiddenText(messages("companyLiabilities.change.hidden"))
        )
      )
    }
}
