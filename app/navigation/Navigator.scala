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
    case CompanyDetailsPage(_)       => _ => routes.ManageCompaniesController.onPageLoad(NormalMode)
    case ManageCompaniesPage         => ua => addCompanyDetails(NormalMode)(ua)
    case SelectActivitiesPage        => _ => routes.ReportAlternativeChargeController.onPageLoad(NormalMode)
    case ReportAlternativeChargePage => ua => reportAlternativeChargeNavigation(NormalMode)(ua)
    case _                           => _ => routes.ReturnsDashboardController.onPageLoad
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

  def navigationForSelectedActivities(selectActivities: Set[SelectActivities], mode: Mode): Call =
    selectActivities match {
      case selectActivities if selectActivities.contains(SelectActivities.SocialMedia)  =>
        ??? // TODO report-search-engine-loss page
      case selectActivities if selectActivities.contains(SelectActivities.SearchEngine) =>
        ??? // TODO report-search-engine-loss page
      case selectActivities if selectActivities.contains(SelectActivities.SocialMedia)  =>
        ??? // TODO report-online-marketplace-loss page
      case selectActivities
          if selectActivities
            .contains(SelectActivities.SocialMedia) && selectActivities.contains(SelectActivities.SearchEngine) =>
        routes.SocialMediaLossController.onPageLoad(mode)
    }

  private def reportAlternativeChargeNavigation(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers.get(SelectActivitiesPage) match {
      case Some(selectActivities) => navigationForSelectedActivities(selectActivities, mode)
      case _                      =>
        routes.ReturnsDashboardController.onPageLoad
    }
}
