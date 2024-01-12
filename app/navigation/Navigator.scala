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

package navigation

import controllers.routes
import models._
import pages.{BankDetailsForRepaymentPage, _}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(periodKey, _)                        =>
      _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode))
    case ManageCompaniesPage(periodKey)                          => ua => addCompanyDetails(periodKey, NormalMode)(ua)
    case SelectActivitiesPage(periodKey)                         =>
      _ => Some(routes.ReportAlternativeChargeController.onPageLoad(periodKey, NormalMode))
    case ReportAlternativeChargePage(periodKey)                  => ua => reportAlternativeChargeNavigation(periodKey, NormalMode)(ua)
    case ReportMediaAlternativeChargePage(periodKey)             => ua => reportMediaAlternative(periodKey, ua)
    case ReportCrossBorderReliefPage(periodKey)                  => ua => reportCrossBorderRelief(periodKey, ua)(NormalMode)
    case ReportSearchAlternativeChargePage(periodKey)            => ua => reportSearchAlternativeCharge(periodKey, ua)(NormalMode)
    case ReportOnlineMarketplaceAlternativeChargePage(periodKey) =>
      ua => reportOnlineMarketplaceCharge(periodKey, ua)(NormalMode)
    case IsRepaymentBankAccountUKPage(periodKey)                 => ua => repaymentBankAccount(periodKey, ua)(NormalMode)
    case CompanyLiabilitiesPage(periodKey, index)                => ua => Some(companyLiability(periodKey, index, ua)(NormalMode))
    case SocialMediaLossPage(periodKey)                          => ua => socialMediaLoss(periodKey, ua)(NormalMode)
    case SearchEngineLossPage(periodKey)                         => ua => searchEngineLoss(periodKey, ua)(NormalMode)
    case GroupLiabilityPage(periodKey)                           => _ => Some(routes.RepaymentController.onPageLoad(periodKey, NormalMode))
    case RepaymentPage(periodKey)                                => ua => repayment(periodKey, ua)(NormalMode)
    case ReportOnlineMarketplaceLossPage(periodKey)              => ua => reportOnlineMarketplaceLoss(periodKey, ua)(NormalMode)
    case ReportOnlineMarketplaceOperatingMarginPage(periodKey)   =>
      _ => Some(routes.ReportCrossBorderReliefController.onPageLoad(periodKey, NormalMode))
    case ReportSocialMediaOperatingMarginPage(periodKey)         => ua => socialMediaOperatingMargin(periodKey, ua)(NormalMode)
    case ReportSearchEngineOperatingMarginPage(periodKey)        => ua => searchEnginOperatingMargin(periodKey, ua)(NormalMode)
    case ReliefDeductedPage(periodKey)                           =>
      _ => Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    case AllowanceDeductedPage(periodKey)                        => ua => Some(companyLiability(periodKey, Index(0), ua)(NormalMode))
    case UKBankDetailsPage(periodKey)                            =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey, isPrint = false))
    case BankDetailsForRepaymentPage(periodKey)                  =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey, isPrint = false))
    case _                                                       => _ => Some(routes.ReturnsDashboardController.onPageLoad)
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(periodKey, _) =>
      _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, CheckMode))
    case SelectActivitiesPage(periodKey)  => ua => reportAlternativeChargeNavigation(periodKey, CheckMode)(ua)
    case _                                => _ => Some(routes.CheckYourAnswersController.onPageLoad(PeriodKey("001"), isPrint = false)) // TODO period key
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
    case CheckMode  =>
      checkRouteMap(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }

  def reportCrossBorderRelief(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportCrossBorderReliefPage(periodKey)) map {
      case true  => routes.ReliefDeductedController.onPageLoad(periodKey, mode)
      case false => routes.AllowanceDeductedController.onPageLoad(periodKey, mode)
    }

  private def addCompanyDetails(periodKey: PeriodKey, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ManageCompaniesPage(periodKey)) map {
      case true  =>
        val count: Int   = userAnswers.get(CompanyDetailsListPage(periodKey)).map(_.size).getOrElse(0)
        val index: Index = Index(count)
        routes.CompanyDetailsController.onPageLoad(periodKey, index, mode)
      case false =>
        routes.SelectActivitiesController.onPageLoad(periodKey, mode)
    }

  private def reportMediaAlternative(periodKey: PeriodKey, ua: UserAnswers): Option[Call] =
    ua.get(ReportMediaAlternativeChargePage(periodKey)) map {
      case true                                                                                                    =>
        routes.SocialMediaLossController.onPageLoad(periodKey, NormalMode)
      case false if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine))      =>
        routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, NormalMode)
      case false if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
        routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, NormalMode)
    }

  private def navigationForSelectedActivitiesYes(
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
    }

  private def redirectToAlternateChargesPage(periodKey: PeriodKey, selectActivities: Set[SelectActivities])(
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

  private def navigationForSelectedActivitiesNo(
    periodKey: PeriodKey,
    selectActivities: Set[SelectActivities],
    mode: Mode
  ): Call =
    selectActivities match {
      case activities if activities.contains(SelectActivities.OnlineMarketplace) =>
        routes.ReportCrossBorderReliefController.onPageLoad(periodKey, mode)
      case _                                                                     =>
        routes.AllowanceDeductedController.onPageLoad(periodKey, mode)
    }

  private def reportAlternativeChargeNavigation(periodKey: PeriodKey, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    (userAnswers.get(SelectActivitiesPage(periodKey)), userAnswers.get(ReportAlternativeChargePage(periodKey))) match {
      case (Some(selectActivities), Some(true))  =>
        Some(navigationForSelectedActivitiesYes(periodKey, selectActivities, mode))
      case (Some(selectActivities), Some(false)) =>
        Some(navigationForSelectedActivitiesNo(periodKey, selectActivities, mode))
      case _                                     => None
    }

  private def repaymentBankAccount(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(IsRepaymentBankAccountUKPage(periodKey))
      .map {
        case true  => routes.UKBankDetailsController.onPageLoad(periodKey, mode)
        case false => routes.BankDetailsForRepaymentController.onPageLoad(periodKey, mode)
      }

  private def companyLiability(periodKey: PeriodKey, pageIndex: Index, ua: UserAnswers)(mode: Mode): Call = {
    val companyDetailsCount: Int = ua.get(CompanyDetailsListPage(periodKey)).map(_.size).getOrElse(0)
    val liabilityCount: Int      = ua.get(CompanyLiabilityListPage(periodKey)).map(_.size).getOrElse(0)

    def preCond(index: Index) = liabilityCount == companyDetailsCount && index.position < liabilityCount
    val index: Index          = if (preCond(pageIndex)) Index(pageIndex.position + 1) else Index(liabilityCount)

    if (liabilityCount < companyDetailsCount || preCond(index)) {
      routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, index)
    } else {
      routes.GroupLiabilityController.onPageLoad(periodKey, mode)
    }
  }

  private def reportSearchAlternativeCharge(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportSearchAlternativeChargePage(periodKey)) map {
      case true  => routes.SearchEngineLossController.onPageLoad(periodKey, mode)
      case false => routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode)
    }

  private def socialMediaLoss(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SocialMediaLossPage(periodKey))
      .map {
        case true if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine)) =>
          routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode)
        case true                                                                                              =>
          companyLiability(periodKey, Index(0), ua)(mode)
        case false                                                                                             =>
          routes.ReportSocialMediaOperatingMarginController.onPageLoad(periodKey, mode)
      }

  private def searchEngineLoss(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call]              =
    ua.get(SearchEngineLossPage(periodKey))
      .map {
        case true if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
          routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode)
        case true                                                                                                   =>
          companyLiability(periodKey, Index(0), ua)(mode)
        case false                                                                                                  =>
          routes.ReportSearchEngineOperatingMarginController.onPageLoad(periodKey, mode)
      }
  private def reportOnlineMarketplaceCharge(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportOnlineMarketplaceAlternativeChargePage(periodKey))
      .map {
        case true  => routes.ReportOnlineMarketplaceLossController.onPageLoad(periodKey, mode)
        case false => routes.ReportCrossBorderReliefController.onPageLoad(periodKey, mode)
      }

  def reportOnlineMarketplaceLoss(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportOnlineMarketplaceLossPage(periodKey))
      .map {
        case true  => routes.ReportCrossBorderReliefController.onPageLoad(periodKey, mode)
        case false => routes.ReportOnlineMarketplaceOperatingMarginController.onPageLoad(periodKey, mode)
      }

  private def repayment(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(RepaymentPage(periodKey))
      .map {
        case true  => routes.IsRepaymentBankAccountUKController.onPageLoad(periodKey, mode)
        case false => routes.CheckYourAnswersController.onPageLoad(periodKey, isPrint = false)
      }

  private def socialMediaOperatingMargin(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine))) {
      Some(routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, mode))
    } else if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode))
    } else {
      Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    }

  private def searchEnginOperatingMargin(periodKey: PeriodKey, ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, mode))
    } else {
      Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    }

}
