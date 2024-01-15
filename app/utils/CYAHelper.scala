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

package utils

import models.{SelectActivities, UserAnswers}
import pages.SelectActivitiesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.Section
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CYAHelper @Inject() () {

  def createSectionList(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] =
    Seq(
      createGroupLiabilitySection(userAnswers),
      createSocialMediaSection(userAnswers),
      createSearchEngineSection(userAnswers),
      createOnlineMarketPlaceSection(userAnswers),
      createReturnRepaymentsSection(userAnswers)
    ).flatten

  private def createGroupLiabilitySection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] =
    buildSection(
      "groupLiability.checkYourAnswersLabel.heading",
      Seq(
        GroupLiabilitySummary.row(userAnswers),
        ReportCrossBorderReliefSummary.row(userAnswers),
        CrossBorderTransactionReliefSummary.row(userAnswers),
        AllowanceDeductedSummary.row(userAnswers)
      )
    )

  private def createSocialMediaSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val selectedValue = userAnswers.get(SelectActivitiesPage).fold(false)(_.contains(SelectActivities.SocialMedia))
    if (selectedValue) {
      buildSection(
        "socialMediaLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(userAnswers, selectedValue),
          ReportAlternativeChargeSummary.row(userAnswers),
          ReportMediaAlternativeChargeSummary.row(userAnswers),
          SocialMediaLossSummary.row(userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createSearchEngineSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val selectedValue = userAnswers.get(SelectActivitiesPage).fold(false)(_.contains(SelectActivities.SearchEngine))
    if (selectedValue) {
      buildSection(
        "searchEngineLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(userAnswers),
          SearchEngineLossSummary.row(userAnswers),
          ReportSearchAlternativeChargeSummary.row(userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createOnlineMarketPlaceSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val selectedValue =
      userAnswers.get(SelectActivitiesPage).fold(false)(_.contains(SelectActivities.OnlineMarketplace))
    if (selectedValue) {
      buildSection(
        "reportOnlineMarketplaceLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(userAnswers),
          ReportOnlineMarketplaceLossSummary.row(userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createReturnRepaymentsSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] =
    buildSection(
      "repayment.checkYourAnswersLabel.heading",
      Seq(
        RepaymentSummary.row(userAnswers),
        IsRepaymentBankAccountUKSummary.row(userAnswers),
        UKBankDetailsSummary.row(userAnswers)
      )
    )

  private def buildSection(heading: String, rows: Seq[Option[SummaryListRow]]): Option[Section] = {
    val nonEmptyRows = rows.flatten
    if (nonEmptyRows.nonEmpty) Some(Section(Some(heading), SummaryListViewModel(nonEmptyRows))) else None
  }
}
