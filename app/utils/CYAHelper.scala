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

import models.{Index, PeriodKey, SelectActivities, UserAnswers}
import pages.SelectActivitiesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.Section
import viewmodels.checkAnswers.{CompanyLiabilitiesSummary, _}
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
  ): Option[Section] = {
    buildSection(
      "groupLiability.checkYourAnswersLabel.heading",
      Seq(
        GroupLiabilitySummary.row(periodKey, userAnswers),
        AllowanceDeductedSummary.row(periodKey, userAnswers),
        ReportCrossBorderReliefSummary.row(periodKey, userAnswers),
        ReliefDeductedSummary.row(periodKey, userAnswers),
        CompanyLiabilitiesSummary.row(periodKey,userAnswers,Index(0))
        // TODO add company liabilities summary here
      )
    )
  }


  private def createSocialMediaSection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] = {
    val selectedValue =
      userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.contains(SelectActivities.SocialMedia))
    if (selectedValue) {
      buildSection(
        "socialMediaLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(periodKey, userAnswers, selectedValue),
          ReportAlternativeChargeSummary.row(periodKey, userAnswers),
          ReportMediaAlternativeChargeSummary.row(periodKey, userAnswers),
          SocialMediaLossSummary.row(periodKey, userAnswers)
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
    if (selectedValue) {
      buildSection(
        "searchEngineLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(periodKey, userAnswers),
          SearchEngineLossSummary.row(periodKey, userAnswers),
          ReportSearchAlternativeChargeSummary.row(periodKey, userAnswers)
        )
      )
    } else {
      None
    }
  }

  private def createOnlineMarketPlaceSection(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[Section] = {
    val selectedValue =
      userAnswers.get(SelectActivitiesPage(periodKey)).fold(false)(_.contains(SelectActivities.OnlineMarketplace))
    if (selectedValue) {
      buildSection(
        "reportOnlineMarketplaceLoss.checkYourAnswersLabel.heading",
        Seq(
          SelectActivitiesSummary.row(periodKey, userAnswers),
          ReportOnlineMarketplaceLossSummary.row(periodKey, userAnswers)
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
