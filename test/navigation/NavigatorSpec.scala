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

import base.SpecBase
import controllers.routes
import models._
import pages._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ReturnsDashboardController.onPageLoad
      }

      "must go from a CompanyDetailsPage to ManageCompanies page" in {

        navigator.nextPage(
          CompanyDetailsPage(index = Index(0)),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ManageCompaniesController.onPageLoad(NormalMode)
      }

      "must go from a ManageCompaniesPage with option 'yes' to ManageCompanies page" in {

        navigator.nextPage(
          ManageCompaniesPage,
          NormalMode,
          UserAnswers("id").set(ManageCompaniesPage, true).success.value
        ) mustBe routes.CompanyDetailsController.onPageLoad(index, NormalMode)
      }

      "must go from a ManageCompaniesPage with option 'false' to Select activities page" in {

        navigator.nextPage(
          ManageCompaniesPage,
          NormalMode,
          UserAnswers("id").set(ManageCompaniesPage, false).success.value
        ) mustBe routes.SelectActivitiesController.onPageLoad(NormalMode)
      }

      "must go from a ManageCompaniesPage to Journey recovery page when data is missing" in {

        navigator.nextPage(
          ManageCompaniesPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "must go from a SelectActivitiesPage to Report alternative charges page" in {

        navigator.nextPage(
          SelectActivitiesPage,
          NormalMode,
          UserAnswers("id").set(SelectActivitiesPage, Set(SelectActivities.values.head)).success.value
        ) mustBe routes.ReportAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportAlternativeChargePage to ReportMediaAlternativeChargePage when more then one activity and option 'yes' is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.OnlineMarketplace)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, true)
            .success
            .value
        ) mustBe routes.ReportMediaAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportAlternativeChargePage to ReportOnlineMarketplaceLossPage when Online marketplace activity option is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.OnlineMarketplace)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, true)
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceLossController.onPageLoad(NormalMode)
      }

      "must go from ReportMediaAlternativeChargePage to SocialMediaLoss page when 'Yes'" in {

        navigator.nextPage(
          ReportMediaAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SocialMedia))
            .success
            .value
            .set(ReportMediaAlternativeChargePage, true)
            .success
            .value
        ) mustBe routes.SocialMediaLossController.onPageLoad(NormalMode)
      }

      // TODO
      "must go from a ReportMediaAlternativeChargePage to report-social-media-loss page when 'No'" ignore {

        navigator.nextPage(
          ReportMediaAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SocialMedia))
            .success
            .value
            .set(ReportMediaAlternativeChargePage, false)
            .success
            .value
        ) mustBe ???
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }

      "must go from a CompanyDetailsPage to ManageCompanies page" in {

        navigator.nextPage(
          CompanyDetailsPage(index = Index(0)),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ManageCompaniesController.onPageLoad(NormalMode)
      }
    }
  }
}
