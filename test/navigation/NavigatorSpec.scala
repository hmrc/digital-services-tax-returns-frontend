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
          CompanyDetailsPage(periodKey, index = Index(0)),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode)
      }

      "must go from a ManageCompaniesPage with option 'yes' to ManageCompanies page" in {

        navigator.nextPage(
          ManageCompaniesPage(periodKey),
          NormalMode,
          UserAnswers("id").set(ManageCompaniesPage(periodKey), true).success.value
        ) mustBe routes.CompanyDetailsController.onPageLoad(periodKey, index, NormalMode)
      }

      "must go from a ManageCompaniesPage with option 'false' to Select activities page" in {

        navigator.nextPage(
          ManageCompaniesPage(periodKey),
          NormalMode,
          UserAnswers("id").set(ManageCompaniesPage(periodKey), false).success.value
        ) mustBe routes.SelectActivitiesController.onPageLoad(NormalMode)
      }

      "must go from a ManageCompaniesPage to Journey recovery page when data is missing" in {

        navigator.nextPage(
          ManageCompaniesPage(periodKey),
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

      "must go from a ReportAlternativeChargePage to ReportSearchAlternativeChargePage when more then one activity and option 'yes' is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.SearchEngine, SelectActivities.OnlineMarketplace)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, true)
            .success
            .value
        ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportAlternativeChargePage to SearchEngineLossPage when Search engine activity option is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.SearchEngine)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, true)
            .success
            .value
        ) mustBe routes.SearchEngineLossController.onPageLoad(periodKey, NormalMode)
      }

      "must go from a ReportAlternativeChargePage to SocialMediaLossPage when Social media activity option is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.SocialMedia)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, true)
            .success
            .value
        ) mustBe routes.SocialMediaLossController.onPageLoad(periodKey, NormalMode)
      }

      "must go from a ReportAlternativeChargePage to ReportCrossBorderReliefPage when OnlineMarketplace is selected and option 'no' is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.OnlineMarketplace, SelectActivities.SearchEngine)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, false)
            .success
            .value
        ) mustBe routes.ReportCrossBorderReliefController.onPageLoad(NormalMode)
      }

      "must go from a ReportAlternativeChargePage to AllowanceDeductedPage when more then one activity is selected and option 'no' is selected" in {

        navigator.nextPage(
          ReportAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(
              SelectActivitiesPage,
              Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
            )
            .success
            .value
            .set(ReportAlternativeChargePage, false)
            .success
            .value
        ) mustBe routes.AllowanceDeductedController.onPageLoad(NormalMode)
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
        ) mustBe routes.SocialMediaLossController.onPageLoad(periodKey, NormalMode)
      }

      "must go from a ReportMediaAlternativeChargePage to ReportSearchAlternativeChargePage when 'No'" in {

        navigator.nextPage(
          ReportMediaAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SearchEngine))
            .success
            .value
            .set(ReportMediaAlternativeChargePage, false)
            .success
            .value
        ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportMediaAlternativeChargePage to ReportOnlineMarketplaceAlternativeChargePage when 'No'" in {

        navigator.nextPage(
          ReportMediaAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.OnlineMarketplace))
            .success
            .value
            .set(ReportMediaAlternativeChargePage, false)
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportCrossBorderReliefPage to AllowanceDeducted page when 'No' is selected" in {

        navigator.nextPage(
          ReportCrossBorderReliefPage,
          NormalMode,
          UserAnswers("id")
            .set(ReportCrossBorderReliefPage, false)
            .success
            .value
        ) mustBe routes.AllowanceDeductedController.onPageLoad(NormalMode)
      }

      "must go from a ReportCrossBorderReliefPage to ReliefDeducted page when 'Yes' is selected" in {

        navigator.nextPage(
          ReportCrossBorderReliefPage,
          NormalMode,
          UserAnswers("id")
            .set(ReportCrossBorderReliefPage, true)
            .success
            .value
        ) mustBe routes.ReliefDeductedController.onPageLoad(NormalMode)
      }

      "must go from a ReportSearchAlternativeChargePage to SearchEngineLoss page when 'Yes' is selected" in {

        navigator.nextPage(
          ReportSearchAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(ReportSearchAlternativeChargePage, true)
            .success
            .value
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SearchEngine))
            .success
            .value
        ) mustBe routes.SearchEngineLossController.onPageLoad(periodKey, NormalMode)
      }

      "must go from a ReportSearchAlternativeChargePage to ReportOnlineMarketplaceAlternativeChargePage when 'No' is selected" in {

        navigator.nextPage(
          ReportSearchAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(ReportSearchAlternativeChargePage, false)
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a SocialMediaLossPage to ReportSearchAlternativeCharge page when 'Yes' is selected and selected activity is 'SearchEngine'" in {

        navigator.nextPage(
          SocialMediaLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SocialMediaLossPage(periodKey), true)
            .success
            .value
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SearchEngine))
            .success
            .value
        ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a SocialMediaLossPage to CompanyLiabilities page when 'Yes' is selected and selected activity is 'SocialMedia'" in {

        navigator.nextPage(
          SocialMediaLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SocialMediaLossPage(periodKey), true)
            .success
            .value
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SocialMedia))
            .success
            .value
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
        ) mustBe routes.CompanyLiabilitiesController.onPageLoad(periodKey, NormalMode, index)
      }

      "must go from a AllowanceDeductedPage to CompanyLiabilities page" in {

        navigator.nextPage(
          AllowanceDeductedPage,
          NormalMode,
          UserAnswers("id")
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.OnlineMarketplace))
            .success
            .value
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
        ) mustBe routes.CompanyLiabilitiesController.onPageLoad(periodKey, NormalMode, index)
      }

      "must go from a SocialMediaLossPage to GroupLiabilityPage when 'Yes' is selected" in {

        navigator.nextPage(
          SocialMediaLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SocialMediaLossPage(periodKey), true)
            .success
            .value
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SocialMedia))
            .success
            .value
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
            .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(100))
            .success
            .value
        ) mustBe routes.GroupLiabilityController.onPageLoad(NormalMode)
      }

      "must go from a SocialMediaLossPage to report-social-media-operating-margin page when 'No' is selected" in {

        navigator.nextPage(
          SocialMediaLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SocialMediaLossPage(periodKey), false)
            .success
            .value
        ) mustBe routes.ReportSocialMediaOperatingMarginController.onPageLoad(NormalMode)
      }

      "must go from a SearchEngineLossPage to ReportOnlineMarketplaceAlternativeCharge page when 'Yes' " +
        "is selected and selected activity is 'OnlineMarketplace'" in {

          navigator.nextPage(
            SearchEngineLossPage(periodKey),
            NormalMode,
            UserAnswers("id")
              .set(SearchEngineLossPage(periodKey), true)
              .success
              .value
              .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.OnlineMarketplace))
              .success
              .value
          ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
        }

      "must go from a SearchEngineLossPage to CompanyLiability page when 'Yes' is selected" in {

        navigator.nextPage(
          SearchEngineLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SearchEngineLossPage(periodKey), true)
            .success
            .value
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
        ) mustBe routes.CompanyLiabilitiesController.onPageLoad(periodKey, NormalMode, index)
      }

      "must go from a SearchEngineLossPage to GroupLiability page when 'Yes' is selected and user has completed the companyDetails page" in {

        navigator.nextPage(
          SearchEngineLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SearchEngineLossPage(periodKey), true)
            .success
            .value
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
            .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(100))
            .success
            .value
        ) mustBe routes.GroupLiabilityController.onPageLoad(NormalMode)
      }

      "must go from a SearchEngineLossPage to report-search-engine-operating-margin page when 'No' is selected" in {

        navigator.nextPage(
          SearchEngineLossPage(periodKey),
          NormalMode,
          UserAnswers("id")
            .set(SearchEngineLossPage(periodKey), false)
            .success
            .value
        ) mustBe routes.ReportSearchEngineOperatingMarginController.onPageLoad(NormalMode)
      }

      "must go from a ReportOnlineMarketplaceAlternativeChargePage to reportOnlineMarketplaceLossPage" +
        " when 'Yes' is selected" in {

          navigator.nextPage(
            ReportOnlineMarketplaceAlternativeChargePage,
            NormalMode,
            UserAnswers("id")
              .set(ReportOnlineMarketplaceAlternativeChargePage, true)
              .success
              .value
          ) mustBe routes.ReportOnlineMarketplaceLossController.onPageLoad(NormalMode)
        }

      "must go from a ReportOnlineMarketplaceAlternativeChargePage to reportCrossBorderTransactionReliefPage when 'No' is selected" in {

        navigator.nextPage(
          ReportOnlineMarketplaceAlternativeChargePage,
          NormalMode,
          UserAnswers("id")
            .set(ReportOnlineMarketplaceAlternativeChargePage, false)
            .success
            .value
        ) mustBe routes.ReportCrossBorderReliefController.onPageLoad(NormalMode)
      }

      "must go from a ReportOnlineMarketplaceLossPage to ReportCrossBorderReliefPage when 'Yes' is selected" in {

        navigator.nextPage(
          ReportOnlineMarketplaceLossPage,
          NormalMode,
          UserAnswers("id")
            .set(ReportOnlineMarketplaceLossPage, true)
            .success
            .value
        ) mustBe routes.ReportCrossBorderReliefController.onPageLoad(NormalMode)
      }

      "must go from a ReportOnlineMarketplaceLossPage to ReportOnlineMarketplaceOperatingMarginPage when 'No' is selected" in {

        navigator.nextPage(
          ReportOnlineMarketplaceLossPage,
          NormalMode,
          UserAnswers("id")
            .set(ReportOnlineMarketplaceLossPage, false)
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceOperatingMarginController.onPageLoad(NormalMode)
      }

      "must go from a ReportOnlineMarketplaceOperatingMarginPage to ReportCrossBorderReliefPage when 'No' is selected" in {

        navigator.nextPage(
          ReportOnlineMarketplaceOperatingMarginPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ReportCrossBorderReliefController.onPageLoad(NormalMode)
      }

      "must go from a ReportSocialMediaOperatingMarginPage to AllowanceDeductedPage" in {

        navigator.nextPage(
          ReportSocialMediaOperatingMarginPage,
          NormalMode,
          emptyUserAnswers.set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SocialMedia)).success.value
        ) mustBe routes.AllowanceDeductedController.onPageLoad(NormalMode)
      }

      "must go from a ReportSocialMediaOperatingMarginPage to ReportSearchAlternativeChargePage when selectedActivities is SearchEngine" in {

        navigator.nextPage(
          ReportSocialMediaOperatingMarginPage,
          NormalMode,
          emptyUserAnswers.set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.SearchEngine)).success.value
        ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportSocialMediaOperatingMarginPage to ReportOnlineMarketplaceAlternativeChargePage when selectedActivities is OnlineMarketplace" in {

        navigator.nextPage(
          ReportSocialMediaOperatingMarginPage,
          NormalMode,
          emptyUserAnswers
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.OnlineMarketplace))
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportSearchEngineOperatingMarginPage to ReportOnlineMarketplaceAlternativeChargePage when selectedActivities is OnlineMarketplace" in {

        navigator.nextPage(
          ReportSearchEngineOperatingMarginPage,
          NormalMode,
          emptyUserAnswers
            .set(SelectActivitiesPage, Set[SelectActivities](SelectActivities.OnlineMarketplace))
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(NormalMode)
      }

      "must go from a ReportSearchEngineOperatingMarginPage to AllowanceDeductedPage" in {

        navigator.nextPage(
          ReportSearchEngineOperatingMarginPage,
          NormalMode,
          emptyUserAnswers
        ) mustBe routes.AllowanceDeductedController.onPageLoad(NormalMode)
      }

      "must go from a IsRepaymentBankAccountUKPage to UKBankDetailsPage when 'Yes' is selected" in {

        navigator.nextPage(
          IsRepaymentBankAccountUKPage,
          NormalMode,
          UserAnswers("id")
            .set(IsRepaymentBankAccountUKPage, true)
            .success
            .value
        ) mustBe routes.UKBankDetailsController.onPageLoad(NormalMode)
      }

      "must go from a IsRepaymentBankAccountUKPage to BankDetailsForRepaymentPage when 'No' is selected" in {

        navigator.nextPage(
          IsRepaymentBankAccountUKPage,
          NormalMode,
          UserAnswers("id")
            .set(IsRepaymentBankAccountUKPage, false)
            .success
            .value
        ) mustBe routes.BankDetailsForRepaymentController.onPageLoad(NormalMode)
      }

      "must go from a GroupLiabilityPage to RepaymentPage" in {

        navigator.nextPage(
          GroupLiabilityPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.RepaymentController.onPageLoad(NormalMode)
      }

      "must go from a RepaymentPage to IsRepaymentBankAccountUKPage when 'Yes' is selected" in {

        navigator.nextPage(
          RepaymentPage,
          NormalMode,
          UserAnswers("id")
            .set(RepaymentPage, true)
            .success
            .value
        ) mustBe routes.IsRepaymentBankAccountUKController.onPageLoad(NormalMode)
      }

      "must go from a RepaymentPage to CheckYourAnswers page when 'No' is selected" in {

        navigator.nextPage(
          RepaymentPage,
          NormalMode,
          UserAnswers("id")
            .set(RepaymentPage, false)
            .success
            .value
        ) mustBe routes.CheckYourAnswersController.onPageLoad(false)
      }

      "must go to CompanyLiabilitiesPage0 index" in {

        navigator.nextPage(
          CompanyLiabilitiesPage(periodKey, index),
          NormalMode,
          UserAnswers("id")
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
            .set(CompanyDetailsPage(periodKey, Index(1)), CompanyDetails("C2", None))
            .success
            .value
        ) mustBe routes.CompanyLiabilitiesController.onPageLoad(periodKey, NormalMode, index)
      }

      "must go from a CompanyLiabilitiesPage0 index to CompanyLiabilitiesPage1 when there are 2 CompanyDetails data exists" in {

        navigator.nextPage(
          CompanyLiabilitiesPage(periodKey, Index(1)),
          NormalMode,
          UserAnswers("id")
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
            .set(CompanyDetailsPage(periodKey, Index(1)), CompanyDetails("C2", None))
            .success
            .value
            .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
            .success
            .value
        ) mustBe routes.CompanyLiabilitiesController.onPageLoad(periodKey, NormalMode, Index(1))
      }

      "must go from a CompanyLiabilitiesPage to GroupLiability page when user filled the pages for both the companies mentioned in manage companies page" in {

        navigator.nextPage(
          CompanyLiabilitiesPage(periodKey, Index(1)),
          NormalMode,
          UserAnswers("id")
            .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
            .success
            .value
            .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
            .success
            .value
        ) mustBe routes.GroupLiabilityController.onPageLoad(NormalMode)
      }
    }

    "must go from a UKBankDetailsPage to CheckYourAnswers page" in {

      navigator.nextPage(
        UKBankDetailsPage,
        NormalMode,
        UserAnswers("id")
      ) mustBe routes.CheckYourAnswersController.onPageLoad(false)
    }

    "must go from a BankDetailsForRepaymentPage to CheckYourAnswers page" in {

      navigator.nextPage(
        BankDetailsForRepaymentPage,
        NormalMode,
        UserAnswers("id")
      ) mustBe routes.CheckYourAnswersController.onPageLoad(false)
    }
  }

}
