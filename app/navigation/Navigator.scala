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
    case CompanyDetailsPage(_)                        => _ => Some(routes.ManageCompaniesController.onPageLoad(NormalMode))
    case ManageCompaniesPage                          => ua => addCompanyDetails(NormalMode)(ua)
    case SelectActivitiesPage                         => _ => Some(routes.ReportAlternativeChargeController.onPageLoad(NormalMode))
    case ReportAlternativeChargePage                  => ua => reportAlternativeChargeNavigation(NormalMode)(ua)
    case ReportMediaAlternativeChargePage             => ua => reportMediaAlternative(ua)
    case ReportCrossBorderReliefPage                  => ua => reportCrossBorderRelief(ua)(NormalMode)
    case ReportSearchAlternativeChargePage            => ua => reportSearchAlternativeCharge(ua)(NormalMode)
    case ReportOnlineMarketplaceAlternativeChargePage => ua => reportOnlineMarketplaceCharge(ua)(NormalMode)
    case IsRepaymentBankAccountUKPage                 => ua => repaymentBankAccount(ua)(NormalMode)
    case UKBankDetailsPage                            => ua => Some(routes.CheckYourAnswersController.onPageLoad())
    case BankDetailsForRepaymentPage                  => ua => Some(routes.CheckYourAnswersController.onPageLoad())
    case CompanyLiabilitiesPage(index)                => ua => Some(companyLiability(index, ua)(NormalMode))
    case SocialMediaLossPage                          => ua => socialMediaLoss(ua)(NormalMode)
    case SearchEngineLossPage                         => ua => searchEngineLoss(ua)(NormalMode)
    case GroupLiabilityPage                           => ua => Some(routes.RepaymentController.onPageLoad(NormalMode))
    case RepaymentPage                                => ua => repayment(ua)(NormalMode)
    case _                                            => _ => Some(routes.ReturnsDashboardController.onPageLoad)
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(_) => _ => Some(routes.ManageCompaniesController.onPageLoad(CheckMode))
    case SelectActivitiesPage  => ua => reportAlternativeChargeNavigation(CheckMode)(ua)
    case _                     => _ => Some(routes.CheckYourAnswersController.onPageLoad())
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
      case true  => routes.SocialMediaLossController.onPageLoad(NormalMode)
      case false => routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
    }

  private def navigationForSelectedActivitiesYes(selectedActivities: Set[SelectActivities], mode: Mode): Call =
    selectedActivities match {
      case activities if activities.size == 1 =>
        activities.head match {
          case SelectActivities.SocialMedia       => routes.SocialMediaLossController.onPageLoad(mode)
          case SelectActivities.SearchEngine      => routes.SearchEngineLossController.onPageLoad(mode)
          case SelectActivities.OnlineMarketplace => routes.ReportOnlineMarketplaceLossController.onPageLoad(mode)
        }
      case selectActivities
          if selectActivities
            .contains(SelectActivities.SocialMedia) && selectActivities.size > 1 =>
        routes.ReportMediaAlternativeChargeController.onPageLoad(mode)
      case _                                  => ??? // TODO implementation is pending for other activities
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

    val preCond      = liabilityCount == companyDetailsCount && pageIndex.position < liabilityCount
    val index: Index = if (preCond) Index(pageIndex.position + 1) else Index(liabilityCount)

    if (
      liabilityCount < companyDetailsCount || liabilityCount == companyDetailsCount && index.position < liabilityCount
    ) {
      routes.CompanyLiabilitiesController.onPageLoad(mode, index)
    } else {
      routes.GroupLiabilityController.onPageLoad(mode)
    }
  }

  private def reportSearchAlternativeCharge(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportSearchAlternativeChargePage) map {
      case true  => routes.SearchEngineLossController.onPageLoad(mode)
      case false => ??? // TODO report-search-engine-operating-margin
    }

  private def socialMediaLoss(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SocialMediaLossPage)
      .map {
        case true if ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.SearchEngine)) =>
          routes.ReportSearchAlternativeChargeController.onPageLoad(mode)
        case true                                                                                   =>
          companyLiability(Index(0), ua)(mode)
        case false                                                                                  => ??? // TODO report-social-media-operating-margin
      }

  private def searchEngineLoss(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(SearchEngineLossPage)
      .map {
        case true if ua.get(SelectActivitiesPage).exists(_.contains(SelectActivities.OnlineMarketplace)) =>
          routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(mode)
        case true                                                                                        =>
          companyLiability(Index(0), ua)(mode)
        case false                                                                                       => ??? // TODO report-social-media-operating-margin
      }

  private def reportOnlineMarketplaceCharge(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(ReportOnlineMarketplaceAlternativeChargePage)
      .map {
        case true  => ??? // TODO report-online-marketplace-loss
        case false => ??? // TODO report-social-media-operating-margin
      }

  private def repayment(ua: UserAnswers)(mode: Mode): Option[Call] =
    ua.get(RepaymentPage)
      .map {
        case true  => routes.IsRepaymentBankAccountUKController.onPageLoad(mode)
        case false => routes.CheckYourAnswersController.onPageLoad()
      }

}
