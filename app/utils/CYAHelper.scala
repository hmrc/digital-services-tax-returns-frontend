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

import models.{PeriodKey, SelectActivities, UserAnswers}
import pages.SelectActivitiesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.Section
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CYAHelper @Inject() () {

  def createSectionList(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] =
    Seq(
      createGroupLiabilitySection(periodKey, userAnswers),
      createSocialMediaSection(periodKey, userAnswers),
      createSearchEngineSection(periodKey, userAnswers),
      createOnlineMarketPlaceSection(periodKey, userAnswers),
      createReturnRepaymentsSection(periodKey, userAnswers)
    ).flatten

  private def createGroupLiabilitySection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] =
    buildSection(
      "groupLiability.checkYourAnswersLabel.heading",
      Seq(
        GroupLiabilitySummary.row(periodKey, userAnswers),
        AllowanceDeductedSummary.row(periodKey, userAnswers),
        ReportCrossBorderReliefSummary.row(periodKey, userAnswers),
        ReliefDeductedSummary.row(periodKey, userAnswers)
        // TODO add company liabilities summary here
      )
    )

  private def createSocialMediaSection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] = {
    val selectedValue =
      userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.contains(SelectActivities.SocialMedia))
    val bool          = userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.size == 1)

    if (selectedValue) {
      buildSection(
        "socialMediaLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(periodKey, userAnswers, selectedValue),
          if (bool) {
            ReportAlternativeChargeSummary.row(periodKey, userAnswers)
          } else {
            ReportMediaAlternativeChargeSummary.row(periodKey, userAnswers)
          },
          SocialMediaLossSummary.row(periodKey, userAnswers),
          ReportSocialMediaOperatingMarginSummary.row(periodKey, userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createSearchEngineSection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] = {
    val selectedValue =
      userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.contains(SelectActivities.SearchEngine))
    val bool          = userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.size == 1)
    if (selectedValue) {
      buildSection(
        "searchEngineLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(periodKey, userAnswers, selectedValue),
          if (bool) {
            ReportAlternativeChargeSummary.row(periodKey, userAnswers)
          } else {
            ReportSearchAlternativeChargeSummary.row(periodKey, userAnswers)
          },
          SearchEngineLossSummary.row(periodKey, userAnswers),
          ReportSearchEngineOperatingMarginSummary.row(periodKey, userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createOnlineMarketPlaceSection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] = {
    val selectedValue: Boolean =
      userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.contains(SelectActivities.OnlineMarketplace))
    val bool                   = userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.size == 1)

    if (selectedValue) {
      buildSection(
        "reportOnlineMarketplaceLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(periodKey, userAnswers, selectedValue),
          if (bool) {
            ReportAlternativeChargeSummary.row(periodKey, userAnswers)
          } else {
            ReportOnlineMarketplaceAlternativeChargeSummary.row(periodKey, userAnswers)
          },
          ReportOnlineMarketplaceLossSummary.row(periodKey, userAnswers),
          ReportOnlineMarketplaceOperatingMarginSummary.row(periodKey, userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createReturnRepaymentsSection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] =
    buildSection(
      "repayment.checkYourAnswersLabel.heading",
      Seq(
        RepaymentSummary.row(periodKey, userAnswers),
        IsRepaymentBankAccountUKSummary.row(periodKey, userAnswers),
        UKBankDetailsSummary.row(periodKey, userAnswers)
      )
    )

  private def buildSection(heading: String, rows: Seq[Option[SummaryListRow]]): Option[Section] = {
    val nonEmptyRows = rows.flatten
    if (nonEmptyRows.nonEmpty) Some(Section(Some(heading), SummaryListViewModel(nonEmptyRows))) else None
  }
}
