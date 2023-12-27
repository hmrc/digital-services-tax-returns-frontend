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
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(_)                           => _ => Some(routes.ManageCompaniesController.onPageLoad(NormalMode))
    case ManageCompaniesPage                             => ua => addCompanyDetails(NormalMode)(ua)
    case SelectActivitiesPage                            => _ => Some(routes.ReportAlternativeChargeController.onPageLoad(NormalMode))
    case ReportAlternativeChargePage                     => ua => reportAlternativeChargeNavigation(NormalMode)(ua)
    case ReportMediaAlternativeChargePage                => ua => reportMediaAlternative(ua)
    case ReportCrossBorderReliefPage                     => ua => reportCrossBorderRelief(ua)(NormalMode)
    case ReportSearchAlternativeChargePage               => ua => reportSearchAlternativeCharge(ua)(NormalMode)
    case ReportOnlineMarketplaceAlternativeChargePage    => ua => reportOnlineMarketplaceCharge(ua)(NormalMode)
    case IsRepaymentBankAccountUKPage                    => ua => repaymentBankAccount(ua)(NormalMode)
    case CompanyLiabilitiesPage(index)                   => ua => Some(companyLiability(index, ua)(NormalMode))
    case SocialMediaLossPage                             => ua => socialMediaLoss(ua)(NormalMode)
    case SearchEngineLossPage                            => ua => searchEngineLoss(ua)(NormalMode)
    case GroupLiabilityPage                              => ua => Some(routes.RepaymentController.onPageLoad(NormalMode))
    case RepaymentPage                                   => ua => repayment(ua)(NormalMode)
    case ReportOnlineMarketplaceLossPage                 => ua => reportOnlineMarketplaceLoss(ua)(NormalMode)
    case ReportOnlineMarketplaceOperatingMarginPage      =>
      ua => Some(routes.ReportCrossBorderReliefController.onPageLoad(NormalMode))
    case ReportSocialMediaOperatingMarginPage            => ua => socialMediaOperatingMargin(ua)(NormalMode)
    case ReportSearchEngineOperatingMarginPage           => ua => searchEnginOperatingMargin(ua)(NormalMode)
    case UKBankDetailsPage | BankDetailsForRepaymentPage =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(false))
    case _                                               => _ => Some(routes.ReturnsDashboardController.onPageLoad)
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(_) => _ => Some(routes.ManageCompaniesController.onPageLoad(CheckMode))
    case SelectActivitiesPage  => ua => reportAlternativeChargeNavigation(CheckMode)(ua)
    case _                     => _ => Some(routes.CheckYourAnswersController.onPageLoad(false))
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
    case CheckMode  =>
      checkRouteMap(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }

  def reportCrossBorderRelief(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportCrossBorderReliefPage) map {
      case true  => ??? // TODO /relief-deducted page
      case false => routes.AllowanceDeductedController.onPageLoad(mode)
    }

  private def addCompanyDetails(mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ManageCompaniesPage) map {
      case true  =>
        val count: Int   = userAnswers.get(CompanyDetailsListPage).map(_.size).getOrElse(0)
        val index: Index = Index(count)
        routes.CompanyDetailsController.onPageLoad(index, mode)
      case false =>
        routes.SelectActivitiesController.onPageLoad(mode)
    }

  private def reportMediaAlternative(ua: UserAnswers): Option[Call] =
    ua.get(ReportMediaAlternativeChargePage) map {
      case true                                                                                         =>
        routes.SocialMediaLossController.onPageLoad(NormalMode)
      case false if ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.SearchEngine))      =>
        routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
      case false if ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
        routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
    }

  private def navigationForSelectedActivitiesYes(selectedActivities: Set[SelectActivities], mode: Mode): Call =
    selectedActivities match {
      case activities if activities.size == 1            =>
        activities.head match {
          case SelectActivities.SocialMedia       => routes.SocialMediaLossController.onPageLoad(mode)
          case SelectActivities.SearchEngine      => routes.SearchEngineLossController.onPageLoad(mode)
          case SelectActivities.OnlineMarketplace => routes.ReportOnlineMarketplaceLossController.onPageLoad(mode)
        }
      case selectActivities if selectActivities.size > 1 =>
        redirectToAlternateChargesPage(selectActivities)(mode)
    }

  private def redirectToAlternateChargesPage(selectActivities: Set[SelectActivities])(mode: Mode): Call =
    if (selectActivities.contains(SelectActivities.SocialMedia)) {
      routes.ReportMediaAlternativeChargeController.onPageLoad(mode)
    } else if (selectActivities.contains(SelectActivities.SearchEngine)) {
      routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
    } else if (selectActivities.contains(SelectActivities.OnlineMarketplace)) {
      routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
    } else {
      routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigationForSelectedActivitiesNo(selectActivities: Set[SelectActivities], mode: Mode): Call =
    selectActivities match {
      case activities if activities.contains(SelectActivities.OnlineMarketplace) =>
        routes.ReportCrossBorderReliefController.onPageLoad(mode)
      case _                                                                     =>
        routes.AllowanceDeductedController.onPageLoad(mode)
    }

  private def reportAlternativeChargeNavigation(mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    (userAnswers.get(SelectActivitiesPage), userAnswers.get(ReportAlternativeChargePage)) match {
      case (Some(selectActivities), Some(true))  => Some(navigationForSelectedActivitiesYes(selectActivities, mode))
      case (Some(selectActivities), Some(false)) => Some(navigationForSelectedActivitiesNo(selectActivities, mode))
      case _                                     => None
    }

  private def repaymentBankAccount(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(IsRepaymentBankAccountUKPage)
      .map {
        case true  => routes.UKBankDetailsController.onPageLoad(mode)
        case false => routes.BankDetailsForRepaymentController.onPageLoad(mode)
      }

  private def companyLiability(pageIndex: Index, ua: UserAnswers)(mode: Mode): Call = {
    val companyDetailsCount: Int = ua.get(CompanyDetailsListPage).map(_.size).getOrElse(0)
    val liabilityCount: Int      = ua.get(CompanyLiabilityListPage).map(_.size).getOrElse(0)

    def preCond(index: Index) = liabilityCount == companyDetailsCount && index.position < liabilityCount
    val index: Index          = if (preCond(pageIndex)) Index(pageIndex.position + 1) else Index(liabilityCount)

    if (liabilityCount < companyDetailsCount || preCond(index)) {
      routes.CompanyLiabilitiesController.onPageLoad(mode, index)
    } else {
      routes.GroupLiabilityController.onPageLoad(mode)
    }
  }

  private def reportSearchAlternativeCharge(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportSearchAlternativeChargePage) map {
      case true  => routes.SearchEngineLossController.onPageLoad(mode)
      case false => routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode)
    }

  private def socialMediaLoss(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SocialMediaLossPage)
      .map {
        case true if ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.SearchEngine)) =>
          routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
        case true                                                                                   =>
          companyLiability(Index(0), ua)(mode)
        case false                                                                                  =>
          routes.ReportSocialMediaOperatingMarginController.onPageLoad(mode)
      }

  private def searchEngineLoss(ua: UserAnswers)(mode: Mode): Option[Call]              =
    ua.get(SearchEngineLossPage)
      .map {
        case true if ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
          routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode)
        case true                                                                                        =>
          companyLiability(Index(0), ua)(mode)
        case false                                                                                       =>
          routes.ReportSearchEngineOperatingMarginController.onPageLoad(mode)
      }
  private def reportOnlineMarketplaceCharge(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportOnlineMarketplaceAlternativeChargePage)
      .map {
        case true  => routes.ReportOnlineMarketplaceLossController.onPageLoad(mode)
        case false => routes.ReportCrossBorderReliefController.onPageLoad(mode)
      }

  def reportOnlineMarketplaceLoss(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportOnlineMarketplaceLossPage)
      .map {
        case true  => routes.ReportCrossBorderReliefController.onPageLoad(mode)
        case false => routes.ReportOnlineMarketplaceOperatingMarginController.onPageLoad(mode)
      }

  private def repayment(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(RepaymentPage)
      .map {
        case true  => routes.IsRepaymentBankAccountUKController.onPageLoad(mode)
        case false => routes.CheckYourAnswersController.onPageLoad(false)
      }

  private def socialMediaOperatingMargin(ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.SearchEngine))) {
      Some(routes.ReportSearchAlternativeChargeController.onPageLoad(mode))
    } else if (ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode))
    } else {
      Some(routes.AllowanceDeductedController.onPageLoad(NormalMode))
    }

  private def searchEnginOperatingMargin(ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode))
    } else {
      Some(routes.AllowanceDeductedController.onPageLoad(NormalMode))
    }

}
