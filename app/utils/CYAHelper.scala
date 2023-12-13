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

import models.SelectActivities
import models.requests.DataRequest
import pages.SelectActivitiesPage
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.Section
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CYAHelper @Inject()() {

  def createSectionList()(implicit request: DataRequest[AnyContent], messages: Messages): Seq[Section] = {
    Seq(
      createGroupLiabilitySection(),
      createSocialMediaSection(),
      createSearchEngineSection(),
      createOnlineMarketPlaceSection(),
      createReturnRepaymentsSection()
    ).flatten
  }

  private def createGroupLiabilitySection()(implicit request: DataRequest[AnyContent], messages: Messages): Option[Section] = {
    buildSection("groupLiability.checkYourAnswersLabel.heading", Seq(
      GroupLiabilitySummary.row(request.userAnswers),
      ReportCrossBorderReliefSummary.row(request.userAnswers),
      CrossBorderTransactionReliefSummary.row(request.userAnswers),
      AllowanceDeductedSummary.row(request.userAnswers)
    ))
  }

  private def createSocialMediaSection()(implicit request: DataRequest[AnyContent], messages: Messages): Option[Section] = {
    val selectedValue = request.userAnswers.get(SelectActivitiesPage).fold(false)(_.contains(SelectActivities.SocialMedia))
    if (selectedValue)
    {
      buildSection("socialMediaLoss.checkYourAnswersLabel.heading", Seq(
        SelectActivitiesSummary.row(request.userAnswers, selectedValue),
        ReportAlternativeChargeSummary.row(request.userAnswers),
        ReportMediaAlternativeChargeSummary.row(request.userAnswers),
        SocialMediaLossSummary.row(request.userAnswers)
      ))
    } else {
      None
    }
  }

  private def createSearchEngineSection()(implicit request: DataRequest[AnyContent], messages: Messages): Option[Section] = {
    val selectedValue = request.userAnswers.get(SelectActivitiesPage).fold(false)(_.contains(SelectActivities.SearchEngine))
    if (selectedValue) {
      buildSection("searchEngineLoss.checkYourAnswersLabel.heading", Seq(
        SelectActivitiesSummary.row(request.userAnswers),
        SearchEngineLossSummary.row(request.userAnswers),
        ReportSearchAlternativeChargeSummary.row(request.userAnswers)
      ))
    } else {
      None
    }
  }

  private def createOnlineMarketPlaceSection()(implicit request: DataRequest[AnyContent], messages: Messages): Option[Section] = {
    val selectedValue = request.userAnswers.get(SelectActivitiesPage).fold(false)(_.contains(SelectActivities.OnlineMarketplace))
    if (selectedValue) {
      buildSection("reportOnlineMarketplaceLoss.checkYourAnswersLabel.heading", Seq(
        SelectActivitiesSummary.row(request.userAnswers),
        ReportOnlineMarketplaceLossSummary.row(request.userAnswers)
      ))
    } else {
      None
    }
  }

  private def createReturnRepaymentsSection()(implicit request: DataRequest[AnyContent], messages: Messages): Option[Section] = {
    buildSection("repayment.checkYourAnswersLabel.heading", Seq(
      RepaymentSummary.row(request.userAnswers),
      IsRepaymentBankAccountUKSummary.row(request.userAnswers),
      UKBankDetailsSummary.row(request.userAnswers)
    ))
  }

  private def buildSection(heading: String, rows: Seq[Option[SummaryListRow]])(implicit request: DataRequest[AnyContent]): Option[Section] = {
    val nonEmptyRows = rows.flatten
    if (nonEmptyRows.nonEmpty) Some(Section(Some(heading), SummaryListViewModel(nonEmptyRows))) else None
  }
}
