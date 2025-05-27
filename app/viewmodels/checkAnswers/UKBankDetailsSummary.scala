/*
 * Copyright 2025 HM Revenue & Customs
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

  def accountNameRow(periodKey: PeriodKey, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UKBankDetailsPage(periodKey)).map { answer =>
      SummaryListRowViewModel(
        key = "uKBankDetails.accountName.change.hidden",
        value = ValueViewModel(HtmlContent(HtmlFormat.escape(answer.accountName).toString)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.UKBankDetailsController.onPageLoad(periodKey, CheckMode).url
          )
            .withVisuallyHiddenText(messages("accountName.change.hidden"))
        )
      )
    }

  def sortCodeRow(periodKey: PeriodKey, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(UKBankDetailsPage(periodKey)).map { answer =>
      SummaryListRowViewModel(
        key = "uKBankDetails.sortCode",
        value = ValueViewModel(HtmlContent(HtmlFormat.escape(answer.sortCode.grouped(2).mkString("-")).toString)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.UKBankDetailsController.onPageLoad(periodKey, CheckMode).url
          )
            .withVisuallyHiddenText(messages("sortCode.change.hidden"))
        )
      )
    }

  def accountNumberRow(periodKey: PeriodKey, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(UKBankDetailsPage(periodKey)).map { answer =>
      SummaryListRowViewModel(
        key = "uKBankDetails.accountNumber",
        value = ValueViewModel(HtmlContent(HtmlFormat.escape(answer.accountNumber).toString)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.UKBankDetailsController.onPageLoad(periodKey, CheckMode).url
          )
            .withVisuallyHiddenText(messages("accountNumber.change.hidden"))
        )
      )
    }

  def buildingNumberRow(periodKey: PeriodKey, answers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    answers.get(UKBankDetailsPage(periodKey)).flatMap { answer =>
      answer.buildingNumber.map { buildingNumber =>
        SummaryListRowViewModel(
          key = "uKBankDetails.buildingRollNumber",
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(buildingNumber).toString)),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.UKBankDetailsController.onPageLoad(periodKey, CheckMode).url
            )
              .withVisuallyHiddenText(messages("uKBankDetails.buildingRollNumber.change.hidden"))
          )
        )
      }
    }
}
