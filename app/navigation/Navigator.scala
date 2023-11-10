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

  private val normalRoutes: Page => UserAnswers => Call = {
    case CompanyDetailsPage(_)            => _ => routes.ManageCompaniesController.onPageLoad(NormalMode)
    case ManageCompaniesPage              => ua => addCompanyDetails(NormalMode)(ua)
    case SelectActivitiesPage             => _ => routes.ReportAlternativeChargeController.onPageLoad(NormalMode)
    case ReportAlternativeChargePage      => ua => reportAlternativeChargeNavigation(NormalMode)(ua)
    case ReportMediaAlternativeChargePage => ua => reportMediaAlternative(ua)
    case _                                => _ => routes.ReturnsDashboardController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case CompanyDetailsPage(_) => _ => routes.ManageCompaniesController.onPageLoad(CheckMode)
    case SelectActivitiesPage  => ua => reportAlternativeChargeNavigation(CheckMode)(ua)
    case _                     => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }

  private def addCompanyDetails(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers.get(ManageCompaniesPage) match {
      case Some(true)  =>
        val count: Int   = userAnswers.get(CompanyDetailsListPage).map(_.size).getOrElse(0)
        val index: Index = Index(count)
        routes.CompanyDetailsController.onPageLoad(index, mode)
      case Some(false) =>
        routes.SelectActivitiesController.onPageLoad(mode)
      case _           => routes.JourneyRecoveryController.onPageLoad()

    }

  private def reportMediaAlternative(ua: UserAnswers): Call =
    ua.get(ReportMediaAlternativeChargePage) match {
      case Some(true)  => routes.SocialMediaLossController.onPageLoad(NormalMode)
      case Some(false) => ??? // TODO report-search-engine-alternative-charge
      case _           => routes.JourneyRecoveryController.onPageLoad()
    }

  private def navigationForSelectedActivitiesYes(selectedActivities: Set[SelectActivities], mode: Mode): Call =
    selectedActivities match {
      case activities if activities.size == 1 =>
        activities.head match {
          case SelectActivities.SocialMedia       => routes.SocialMediaLossController.onPageLoad(mode)
          case SelectActivities.SearchEngine      => ??? // TODO report-search-engine-loss
          case SelectActivities.OnlineMarketplace => ??? // TODO report-online-marketplace-loss
        }
      case selectActivities
          if selectActivities
            .contains(SelectActivities.SocialMedia) && selectActivities.size > 1 =>
        routes.ReportMediaAlternativeChargeController.onPageLoad(mode)
    }

  private def navigationForSelectedActivitiesNo(selectActivities: Set[SelectActivities], mode: Mode): Call =
    selectActivities match {
      case activities if activities.contains(SelectActivities.OnlineMarketplace) =>
        routes.ReportCrossBorderReliefController.onPageLoad(mode)
      case _                                                                     =>
        routes.AllowanceDeductedController.onPageLoad(mode)
    }

  private def reportAlternativeChargeNavigation(mode: Mode)(userAnswers: UserAnswers): Call =
    (userAnswers.get(SelectActivitiesPage), userAnswers.get(ReportAlternativeChargePage)) match {
      case (Some(selectActivities), Some(true))  => navigationForSelectedActivitiesYes(selectActivities, mode)
      case (Some(selectActivities), Some(false)) => navigationForSelectedActivitiesNo(selectActivities, mode)
      case _                                     =>
        routes.ReturnsDashboardController.onPageLoad
    }
}
