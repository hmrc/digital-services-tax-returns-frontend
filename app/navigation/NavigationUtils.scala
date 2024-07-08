/*
 * Copyright 2024 HM Revenue & Customs
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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

trait NavigationUtils {

  private[navigation] def reportCrossBorderRelief(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    (
      ua.get(ReportCrossBorderReliefPage(periodKey)),
      ua.get(ReportAlternativeChargePage(periodKey)),
      isAlternativeChargesSelected(periodKey, ua)
    ) match {
      case (Some(false), Some(true), true) =>
        Some(routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, Index(0)))
      case (Some(false), Some(_), _)       => Some(routes.AllowanceDeductedController.onPageLoad(periodKey, mode))
      case (Some(true), Some(_), _)        => Some(routes.ReliefDeductedController.onPageLoad(periodKey, mode))
      case _                               => None
    }

  private def isAlternativeChargesSelected(periodKey: PeriodKey, ua: UserAnswers): Boolean = {
    val pages = Seq(SocialMediaLossPage, SearchEngineLossPage, ReportOnlineMarketplaceLossPage)
    pages.exists(page => ua.get(page(periodKey)).contains(true))
  }

  private[navigation] def reliefDeducted(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    (ua.get(ReportAlternativeChargePage(periodKey)), isAlternativeChargesSelected(periodKey, ua)) match {
      case (Some(true), true) => Some(routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, Index(0)))
      case (Some(_), _)       => Some(routes.AllowanceDeductedController.onPageLoad(periodKey, mode))
      case _                  => None
    }

  private[navigation] def addCompanyDetails(periodKey: PeriodKey, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ManageCompaniesPage(periodKey)) map {
      case true  =>
        val count: Int   = userAnswers.get(CompanyDetailsListPage(periodKey)).map(_.size).getOrElse(0)
        val index: Index = Index(count)
        routes.CompanyDetailsController.onPageLoad(periodKey, index, mode)
      case false =>
        routes.SelectActivitiesController.onPageLoad(periodKey, mode)
    }

  private[navigation] def reportMediaAlternative(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportMediaAlternativeChargePage(periodKey)) map {
      case true                                                                                                    =>
        routes.SocialMediaLossController.onPageLoad(periodKey, mode)
      case false if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine))      =>
        routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode)
      case false if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
        routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode)
    }

  private[navigation] def navigationForSelectedActivitiesYes(
    periodKey: PeriodKey,
    selectedActivities: Set[SelectActivities],
    mode: Mode
  ): Call =
    selectedActivities match {
      case activities if activities.size == 1            =>
        activities.head match {
          case SelectActivities.SocialMedia       => routes.SocialMediaLossController.onPageLoad(periodKey, mode)
          case SelectActivities.SearchEngine      => routes.SearchEngineLossController.onPageLoad(periodKey, mode)
          case SelectActivities.OnlineMarketplace =>
            routes.ReportOnlineMarketplaceLossController.onPageLoad(periodKey, mode)
        }
      case selectActivities if selectActivities.size > 1 =>
        redirectToAlternateChargesPage(periodKey, selectActivities)(mode)
      case _                                             =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  private[navigation] def redirectToAlternateChargesPage(periodKey: PeriodKey, selectActivities: Set[SelectActivities])(
    mode: Mode
  ): Call =
    if (selectActivities.contains(SelectActivities.SocialMedia)) {
      routes.ReportMediaAlternativeChargeController.onPageLoad(periodKey, mode)
    } else if (selectActivities.contains(SelectActivities.SearchEngine)) {
      routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode)
    } else if (selectActivities.contains(SelectActivities.OnlineMarketplace)) {
      routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode)
    } else {
      routes.JourneyRecoveryController.onPageLoad()
    }

  private[navigation] def navigationForSelectedActivitiesNo(
    periodKey: PeriodKey,
    selectActivities: Set[SelectActivities],
    mode: Mode
  ): Call =
    selectActivities match {
      case activities if activities.contains(SelectActivities.OnlineMarketplace) && mode == NormalMode =>
        routes.ReportCrossBorderReliefController.onPageLoad(periodKey, mode)
      case _ if mode == NormalMode                                                                     =>
        routes.AllowanceDeductedController.onPageLoad(periodKey, mode)
      case _                                                                                           =>
        routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

  private[navigation] def reportAlternativeChargeNavigation(periodKey: PeriodKey, mode: Mode)(
    userAnswers: UserAnswers
  ): Option[Call] =
    (userAnswers.get(SelectActivitiesPage(periodKey)), userAnswers.get(ReportAlternativeChargePage(periodKey))) match {
      case (Some(selectActivities), Some(true))  =>
        Some(navigationForSelectedActivitiesYes(periodKey, selectActivities, mode))
      case (Some(selectActivities), Some(false)) =>
        Some(navigationForSelectedActivitiesNo(periodKey, selectActivities, mode))
      case _                                     => None
    }

  private[navigation] def repaymentBankAccount(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(IsRepaymentBankAccountUKPage(periodKey))
      .map {
        case true  => routes.UKBankDetailsController.onPageLoad(periodKey, mode)
        case false => routes.BankDetailsForRepaymentController.onPageLoad(periodKey, mode)
      }

  private[navigation] def companyLiability(periodKey: PeriodKey, pageIndex: Index, ua: UserAnswers)(
    mode: Mode
  ): Call = {
    val companyDetailsCount: Int       = ua.get(CompanyDetailsListPage(periodKey)).map(_.size).getOrElse(0)
    val liabilityCount: Int            = ua.get(CompanyLiabilityListPage(periodKey)).map(_.size).getOrElse(0)
    def preCond(index: Index): Boolean = liabilityCount == companyDetailsCount && index.position < liabilityCount

    val index: Index = if (preCond(pageIndex)) Index(pageIndex.position + 1) else Index(liabilityCount)

    if (liabilityCount < companyDetailsCount || preCond(index)) {
      routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, index)
    } else {
      routes.GroupLiabilityController.onPageLoad(periodKey, mode)
    }
  }

  private[navigation] def reportSearchAlternativeCharge(periodKey: PeriodKey, ua: UserAnswers)(
    mode: Mode
  ): Option[Call] =
    ua.get(ReportSearchAlternativeChargePage(periodKey)) map {
      case true  => routes.SearchEngineLossController.onPageLoad(periodKey, mode)
      case false => routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode)
    }

  private[navigation] def socialMediaLoss(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SocialMediaLossPage(periodKey))
      .map {
        case true if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine)) =>
          routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode)
        case true if mode == NormalMode                                                                        =>
          routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, Index(0))
        case true if mode == CheckMode                                                                         =>
          routes.CheckYourAnswersController.onPageLoad(periodKey)
        case _                                                                                                 =>
          routes.ReportSocialMediaOperatingMarginController.onPageLoad(periodKey, mode)
      }

  private[navigation] def searchEngineLoss(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SearchEngineLossPage(periodKey))
      .map {
        case true if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
          routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode)
        case true if mode == NormalMode                                                                             =>
          routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, Index(0))
        case true if mode == CheckMode                                                                              =>
          routes.CheckYourAnswersController.onPageLoad(periodKey)
        case _                                                                                                      =>
          routes.ReportSearchEngineOperatingMarginController.onPageLoad(periodKey, mode)
      }

  private[navigation] def reportOnlineMarketplaceCharge(periodKey: PeriodKey, ua: UserAnswers)(
    mode: Mode
  ): Option[Call] =
    ua.get(ReportOnlineMarketplaceAlternativeChargePage(periodKey))
      .map {
        case true                        => routes.ReportOnlineMarketplaceLossController.onPageLoad(periodKey, mode)
        case false if mode == NormalMode => routes.ReportCrossBorderReliefController.onPageLoad(periodKey, mode)
        case _                           => routes.CheckYourAnswersController.onPageLoad(periodKey)
      }

  def reportOnlineMarketplaceLoss(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportOnlineMarketplaceLossPage(periodKey))
      .map {
        case true if mode == NormalMode => routes.ReportCrossBorderReliefController.onPageLoad(periodKey, mode)
        case true if mode == CheckMode  => routes.CheckYourAnswersController.onPageLoad(periodKey)
        case _                          => routes.ReportOnlineMarketplaceOperatingMarginController.onPageLoad(periodKey, mode)
      }

  private[navigation] def repayment(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(RepaymentPage(periodKey))
      .map {
        case true  => routes.IsRepaymentBankAccountUKController.onPageLoad(periodKey, mode)
        case false => routes.CheckYourAnswersController.onPageLoad(periodKey)
      }

  private[navigation] def socialMediaOperatingMargin(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine))) {
      Some(routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode))
    } else if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode))
    } else if (mode == NormalMode) {
      Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    } else {
      Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    }

  private[navigation] def searchEnginOperatingMargin(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode))
    } else if (mode == NormalMode) {
      Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    } else {
      Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    }
}
