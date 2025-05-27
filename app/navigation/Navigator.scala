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

package navigation

import controllers.routes
import models._
import pages.{BankDetailsForRepaymentPage, ResubmitAReturnPage, _}
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () extends NavigationUtils {

  private val normalRoutes: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(periodKey, _)                        =>
      _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode))
    case ManageCompaniesPage(periodKey)                          => ua => addCompanyDetails(periodKey, NormalMode)(ua)
    case SelectActivitiesPage(periodKey)                         =>
      _ => Some(routes.ReportAlternativeChargeController.onPageLoad(periodKey, NormalMode))
    case ReportAlternativeChargePage(periodKey)                  => ua => reportAlternativeChargeNavigation(periodKey, NormalMode)(ua)
    case ReportMediaAlternativeChargePage(periodKey)             => ua => reportMediaAlternative(periodKey, ua)(NormalMode)
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
      ua => reliefDeducted(periodKey, ua)(NormalMode)
    case AllowanceDeductedPage(periodKey)                        =>
      _ => Some(routes.CompanyLiabilitiesController.onPageLoad(periodKey, NormalMode, Index(0)))
    case UKBankDetailsPage(periodKey)                            =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    case BankDetailsForRepaymentPage(periodKey)                  =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    case ResubmitAReturnPage(periodKey)                          => _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode))
    case _                                                       => _ => Some(routes.ReturnsDashboardController.onPageLoad)
  }

  private val checkRouteMap: Page => UserAnswers => Option[Call] = {
    case CompanyDetailsPage(periodKey, _)                        =>
      _ => Some(routes.ManageCompaniesController.onPageLoad(periodKey, CheckMode))
    case SelectActivitiesPage(periodKey)                         =>
      _ => Some(routes.ReportAlternativeChargeController.onPageLoad(periodKey, CheckMode))
    case GroupLiabilityPage(periodKey)                           => _ => Some(routes.RepaymentController.onPageLoad(periodKey, CheckMode))
    case CompanyLiabilitiesPage(periodKey, index)                => ua => Some(companyLiability(periodKey, index, ua)(CheckMode))
    case ReportAlternativeChargePage(periodKey)                  => ua => reportAlternativeChargeNavigation(periodKey, CheckMode)(ua)
    case ReportMediaAlternativeChargePage(periodKey)             => ua => reportMediaAlternative(periodKey, ua)(CheckMode)
    case ReportCrossBorderReliefPage(periodKey)                  => ua => reportCrossBorderRelief(periodKey, ua)(CheckMode)
    case ReportSearchAlternativeChargePage(periodKey)            => ua => reportSearchAlternativeCharge(periodKey, ua)(CheckMode)
    case ReportOnlineMarketplaceAlternativeChargePage(periodKey) =>
      ua => reportOnlineMarketplaceCharge(periodKey, ua)(CheckMode)
    case SocialMediaLossPage(periodKey)                          => ua => socialMediaLoss(periodKey, ua)(CheckMode)
    case SearchEngineLossPage(periodKey)                         => ua => searchEngineLoss(periodKey, ua)(CheckMode)
    case ReportOnlineMarketplaceLossPage(periodKey)              => ua => reportOnlineMarketplaceLoss(periodKey, ua)(CheckMode)
    case ReportOnlineMarketplaceOperatingMarginPage(periodKey)   =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    case ReportSocialMediaOperatingMarginPage(periodKey)         => ua => socialMediaOperatingMargin(periodKey, ua)(CheckMode)
    case ReportSearchEngineOperatingMarginPage(periodKey)        => ua => searchEnginOperatingMargin(periodKey, ua)(CheckMode)
    case ReliefDeductedPage(periodKey)                           =>
      ua => reliefDeducted(periodKey, ua)(CheckMode)
    case RepaymentPage(periodKey)                                => ua => repayment(periodKey, ua)(CheckMode)
    case IsRepaymentBankAccountUKPage(periodKey)                 => ua => repaymentBankAccount(periodKey, ua)(CheckMode)
    case UKBankDetailsPage(periodKey)                            =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    case BankDetailsForRepaymentPage(periodKey)                  =>
      _ => Some(routes.CheckYourAnswersController.onPageLoad(periodKey))
    case _                                                       =>
      _ => None
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
    case CheckMode  =>
      checkRouteMap(page)(userAnswers).getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }
}
