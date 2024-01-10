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
    case CompanyDetailsPage(periodKey, _)                => _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode))
    case ManageCompaniesPage(periodKey)                  => ua => addCompanyDetails(periodKey, NormalMode)(ua)
    case SelectActivitiesPage(periodKey)                 => _ => Some(routes.ReportAlternativeChargeController.onPageLoad(periodKey, NormalMode))
    case ReportAlternativeChargePage(periodKey)          => ua => reportAlternativeChargeNavigation(periodKey, NormalMode)(ua)
    case ReportMediaAlternativeChargePage(periodKey)     => ua => reportMediaAlternative(periodKey, ua)
    case ReportCrossBorderReliefPage                     => ua => reportCrossBorderRelief("periodKey", ua)(NormalMode) //TODO
    case ReportSearchAlternativeChargePage               => ua => reportSearchAlternativeCharge(ua)(NormalMode)
    case ReportOnlineMarketplaceAlternativeChargePage    => ua => reportOnlineMarketplaceCharge(ua)(NormalMode)
    case IsRepaymentBankAccountUKPage                    => ua => repaymentBankAccount(ua)(NormalMode)
    case CompanyLiabilitiesPage(periodKey, index)        => ua => Some(companyLiability(periodKey, index, ua)(NormalMode))
    case SocialMediaLossPage(periodKey)                  => ua => socialMediaLoss(periodKey, ua)(NormalMode)
    case SearchEngineLossPage(periodKey)                 => ua => searchEngineLoss(periodKey, ua)(NormalMode)
    case GroupLiabilityPage                              => ua => Some(routes.RepaymentController.onPageLoad(NormalMode))
    case RepaymentPage                                   => ua => repayment(ua)(NormalMode)
    case ReportOnlineMarketplaceLossPage                 => ua => reportOnlineMarketplaceLoss(ua)(NormalMode)
    case ReportOnlineMarketplaceOperatingMarginPage      =>
      ua => Some(routes.ReportCrossBorderReliefController.onPageLoad(NormalMode))
    case ReportSocialMediaOperatingMarginPage(periodKey)            => ua => socialMediaOperatingMargin(periodKey, ua)(NormalMode)
    case ReportSearchEngineOperatingMarginPage(periodKey)           => ua => searchEnginOperatingMargin(periodKey, ua)(NormalMode)
    case ReliefDeductedPage                              => _ => Some(routes.AllowanceDeductedController.onPageLoad("periodKey", NormalMode))//TODO
    case AllowanceDeductedPage(periodKey)                => ua => Some(companyLiability(periodKey, Index(0), ua)(NormalMode))
    case UKBankDetailsPage | BankDetailsForRepaymentPage =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(false))
    case _                                               => _ => Some(routes.ReturnsDashboardController.onPageLoad)
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(periodKey,_) => _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, CheckMode))
    case SelectActivitiesPage(periodKey)  => ua => reportAlternativeChargeNavigation(periodKey, CheckMode)(ua)
    case _                     => _ => Some(routes.CheckYourAnswersController.onPageLoad(false))
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
    case CheckMode  =>
      checkRouteMap(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }

  def reportCrossBorderRelief(periodKey: String, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportCrossBorderReliefPage) map {
      case true  => routes.ReliefDeductedController.onPageLoad(mode)
      case false => routes.AllowanceDeductedController.onPageLoad(periodKey, mode)
    }

  private def addCompanyDetails(periodKey: String, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    userAnswers.get(ManageCompaniesPage(periodKey)) map {
      case true  =>
        val count: Int   = userAnswers.get(CompanyDetailsListPage(periodKey)).map(_.size).getOrElse(0)
        val index: Index = Index(count)
        routes.CompanyDetailsController.onPageLoad(periodKey, index, mode)
      case false =>
        routes.SelectActivitiesController.onPageLoad(periodKey, mode)
    }

  private def reportMediaAlternative(periodKey: String, ua: UserAnswers): Option[Call] =
    ua.get(ReportMediaAlternativeChargePage(periodKey)) map {
      case true                                                                                         =>
        routes.SocialMediaLossController.onPageLoad(periodKey, NormalMode)
      case false if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine))      =>
        routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
      case false if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
        routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
    }

  private def navigationForSelectedActivitiesYes(periodKey: String, selectedActivities: Set[SelectActivities], mode: Mode): Call =
    selectedActivities match {
      case activities if activities.size == 1            =>
        activities.head match {
          case SelectActivities.SocialMedia       => routes.SocialMediaLossController.onPageLoad(periodKey, mode)
          case SelectActivities.SearchEngine      => routes.SearchEngineLossController.onPageLoad(periodKey, mode)
          case SelectActivities.OnlineMarketplace => routes.ReportOnlineMarketplaceLossController.onPageLoad(mode)
        }
      case selectActivities if selectActivities.size > 1 =>
        redirectToAlternateChargesPage(periodKey, selectActivities)(mode)
    }

  private def redirectToAlternateChargesPage(periodKey: String, selectActivities: Set[SelectActivities])(mode: Mode): Call =
    if (selectActivities.contains(SelectActivities.SocialMedia)) {
      routes.ReportMediaAlternativeChargeController.onPageLoad(periodKey, mode)
    } else if (selectActivities.contains(SelectActivities.SearchEngine)) {
      routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
    } else if (selectActivities.contains(SelectActivities.OnlineMarketplace)) {
      routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
    } else {
      routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigationForSelectedActivitiesNo(periodKey: String, selectActivities: Set[SelectActivities], mode: Mode): Call =
    selectActivities match {
      case activities if activities.contains(SelectActivities.OnlineMarketplace) =>
        routes.ReportCrossBorderReliefController.onPageLoad(mode)
      case _                                                                     =>
        routes.AllowanceDeductedController.onPageLoad(periodKey, mode)
    }

  private def reportAlternativeChargeNavigation(periodKey: String, mode: Mode)(userAnswers: UserAnswers): Option[Call] =
    (userAnswers.get(SelectActivitiesPage(periodKey)), userAnswers.get(ReportAlternativeChargePage(periodKey))) match {
      case (Some(selectActivities), Some(true))  => Some(navigationForSelectedActivitiesYes(periodKey, selectActivities, mode))
      case (Some(selectActivities), Some(false)) => Some(navigationForSelectedActivitiesNo(periodKey, selectActivities, mode))
      case _                                     => None
    }

  private def repaymentBankAccount(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(IsRepaymentBankAccountUKPage)
      .map {
        case true  => routes.UKBankDetailsController.onPageLoad(mode)
        case false => routes.BankDetailsForRepaymentController.onPageLoad(mode)
      }

  private def companyLiability(periodKey: String, pageIndex: Index, ua: UserAnswers)(mode: Mode): Call = {
    val companyDetailsCount: Int = ua.get(CompanyDetailsListPage(periodKey)).map(_.size).getOrElse(0)
    val liabilityCount: Int      = ua.get(CompanyLiabilityListPage(periodKey)).map(_.size).getOrElse(0)

    def preCond(index: Index) = liabilityCount == companyDetailsCount && index.position < liabilityCount
    val index: Index          = if (preCond(pageIndex)) Index(pageIndex.position + 1) else Index(liabilityCount)

    if (liabilityCount < companyDetailsCount || preCond(index)) {
      routes.CompanyLiabilitiesController.onPageLoad(periodKey, mode, index)
    } else {
      routes.GroupLiabilityController.onPageLoad(mode)
    }
  }

  private def reportSearchAlternativeCharge(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportSearchAlternativeChargePage) map {
      case true  => routes.SearchEngineLossController.onPageLoad("001", mode) //TODO
      case false => routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode)
    }

  private def socialMediaLoss(periodKey: String, ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SocialMediaLossPage(periodKey))
      .map {
        case true if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine)) =>
          routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
        case true                                                                                   =>
          companyLiability(periodKey, Index(0), ua)(mode)
        case false                                                                                  =>
          routes.ReportSocialMediaOperatingMarginController.onPageLoad(periodKey, mode)
      }

  private def searchEngineLoss(periodKey: String, ua: UserAnswers)(mode: Mode): Option[Call]              =
    ua.get(SearchEngineLossPage(periodKey))
      .map {
        case true if ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
          routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode)
        case true                                                                                        =>
          companyLiability(periodKey, Index(0), ua)(mode)
        case false                                                                                       =>
          routes.ReportSearchEngineOperatingMarginController.onPageLoad(periodKey, mode)
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

  private def socialMediaOperatingMargin(periodKey: String, ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.SearchEngine))) {
      Some(routes.ReportSearchAlternativeChargeController.onPageLoad(mode))
    } else if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode))
    } else {
      Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    }

  private def searchEnginOperatingMargin(periodKey: String, ua: UserAnswers)(mode: Mode): Option[Call] =
    if (ua.get(SelectActivitiesPage(periodKey)).exists(_.contains(SelectActivities.OnlineMarketplace))) {
      Some(routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode))
    } else {
      Some(routes.AllowanceDeductedController.onPageLoad(periodKey, NormalMode))
    }

}
