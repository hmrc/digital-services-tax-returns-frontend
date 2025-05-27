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

import base.SpecBase
import controllers.routes
import models._
import pages._

class CheckModeNavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator in Check mode" - {

    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        CheckMode,
        UserAnswers("id")
      ) mustBe routes.JourneyRecoveryController.onPageLoad()
    }

    "must go from a CompanyDetailsPage to ManageCompanies page" in {

      navigator.nextPage(
        CompanyDetailsPage(periodKey, index = Index(0)),
        CheckMode,
        UserAnswers("id")
      ) mustBe routes.ManageCompaniesController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a SelectActivitiesPage to Report alternative charges page" in {

      navigator.nextPage(
        SelectActivitiesPage(periodKey),
        CheckMode,
        UserAnswers("id").set(SelectActivitiesPage(periodKey), Set(SelectActivities.values.head)).success.value
      ) mustBe routes.ReportAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportAlternativeChargePage to ReportMediaAlternativeChargePage when more then one activity and option 'yes' is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.OnlineMarketplace)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.ReportMediaAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportAlternativeChargePage to ReportSearchAlternativeChargePage when more then one activity and option 'yes' is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.SearchEngine, SelectActivities.OnlineMarketplace)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportAlternativeChargePage to SearchEngineLossPage when Search engine activity option is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.SearchEngine)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.SearchEngineLossController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportAlternativeChargePage to SocialMediaLossPage when Social media activity option is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.SocialMedia)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.SocialMediaLossController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportAlternativeChargePage to CheckYourAnswers page when OnlineMarketplace is selected and option 'no' is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.OnlineMarketplace, SelectActivities.SearchEngine)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), false)
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a ReportAlternativeChargePage to CheckYourAnswers page when more then one activity is selected and option 'no' is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), false)
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a ReportAlternativeChargePage to ReportOnlineMarketplaceLossPage when Online marketplace activity option is selected" in {

      navigator.nextPage(
        ReportAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            SelectActivitiesPage(periodKey),
            Set[SelectActivities](SelectActivities.OnlineMarketplace)
          )
          .success
          .value
          .set(ReportAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceLossController.onPageLoad(periodKey, CheckMode)
    }

    "must go from ReportMediaAlternativeChargePage to SocialMediaLoss page when 'Yes'" in {

      navigator.nextPage(
        ReportMediaAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SocialMedia))
          .success
          .value
          .set(ReportMediaAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.SocialMediaLossController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportMediaAlternativeChargePage to ReportSearchAlternativeChargePage when 'No'" in {

      navigator.nextPage(
        ReportMediaAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SearchEngine))
          .success
          .value
          .set(ReportMediaAlternativeChargePage(periodKey), false)
          .success
          .value
      ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportMediaAlternativeChargePage to ReportOnlineMarketplaceAlternativeChargePage when 'No'" in {

      navigator.nextPage(
        ReportMediaAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.OnlineMarketplace))
          .success
          .value
          .set(ReportMediaAlternativeChargePage(periodKey), false)
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportSearchAlternativeChargePage to SearchEngineLoss page when 'Yes' is selected" in {

      navigator.nextPage(
        ReportSearchAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(ReportSearchAlternativeChargePage(periodKey), true)
          .success
          .value
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SearchEngine))
          .success
          .value
      ) mustBe routes.SearchEngineLossController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportSearchAlternativeChargePage to ReportOnlineMarketplaceAlternativeChargePage when 'No' is selected" in {

      navigator.nextPage(
        ReportSearchAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(ReportSearchAlternativeChargePage(periodKey), false)
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a SocialMediaLossPage to ReportSearchAlternativeCharge page when 'Yes' is selected and selected activity is 'SearchEngine'" in {

      navigator.nextPage(
        SocialMediaLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SocialMediaLossPage(periodKey), true)
          .success
          .value
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SearchEngine))
          .success
          .value
      ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a SocialMediaLossPage to CheckYourAnswers page when 'Yes' is selected and selected activity is 'SocialMedia'" in {

      navigator.nextPage(
        SocialMediaLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SocialMediaLossPage(periodKey), true)
          .success
          .value
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SocialMedia))
          .success
          .value
          .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a SocialMediaLossPage to report-social-media-operating-margin page when 'No' is selected" in {

      navigator.nextPage(
        SocialMediaLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SocialMediaLossPage(periodKey), false)
          .success
          .value
      ) mustBe routes.ReportSocialMediaOperatingMarginController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a SearchEngineLossPage to ReportOnlineMarketplaceAlternativeCharge page when 'Yes' " +
      "is selected and selected activity is 'OnlineMarketplace'" in {

        navigator.nextPage(
          SearchEngineLossPage(periodKey),
          CheckMode,
          UserAnswers("id")
            .set(SearchEngineLossPage(periodKey), true)
            .success
            .value
            .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.OnlineMarketplace))
            .success
            .value
        ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, CheckMode)
      }

    "must go from a SearchEngineLossPage to CompanyLiability page when 'Yes' is selected" in {

      navigator.nextPage(
        SearchEngineLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SearchEngineLossPage(periodKey), true)
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a SearchEngineLossPage to report-search-engine-operating-margin page when 'No' is selected" in {

      navigator.nextPage(
        SearchEngineLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(SearchEngineLossPage(periodKey), false)
          .success
          .value
      ) mustBe routes.ReportSearchEngineOperatingMarginController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportOnlineMarketplaceAlternativeChargePage to reportOnlineMarketplaceLossPage when 'Yes' is selected" in {

      navigator.nextPage(
        ReportOnlineMarketplaceAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(ReportOnlineMarketplaceAlternativeChargePage(periodKey), true)
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceLossController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportOnlineMarketplaceAlternativeChargePage to CheckYourAnswers Page when 'No' is selected" in {

      navigator.nextPage(
        ReportOnlineMarketplaceAlternativeChargePage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(ReportOnlineMarketplaceAlternativeChargePage(periodKey), false)
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a ReportOnlineMarketplaceLossPage to CheckYourAnswers page when 'Yes' is selected" in {

      navigator.nextPage(
        ReportOnlineMarketplaceLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(ReportOnlineMarketplaceLossPage(periodKey), true)
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a ReportOnlineMarketplaceLossPage to ReportOnlineMarketplaceOperatingMarginPage when 'No' is selected" in {

      navigator.nextPage(
        ReportOnlineMarketplaceLossPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(ReportOnlineMarketplaceLossPage(periodKey), false)
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceOperatingMarginController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportOnlineMarketplaceOperatingMarginPage to CheckYourAnswerPage when 'No' is selected" in {

      navigator.nextPage(
        ReportOnlineMarketplaceOperatingMarginPage(periodKey),
        CheckMode,
        UserAnswers("id")
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a ReportSocialMediaOperatingMarginPage to CheckYourAnswers page" in {

      navigator.nextPage(
        ReportSocialMediaOperatingMarginPage(periodKey),
        CheckMode,
        emptyUserAnswers
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SocialMedia))
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a ReportSocialMediaOperatingMarginPage to ReportSearchAlternativeChargePage when selectedActivities is SearchEngine" in {

      navigator.nextPage(
        ReportSocialMediaOperatingMarginPage(periodKey),
        CheckMode,
        emptyUserAnswers
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SearchEngine))
          .success
          .value
      ) mustBe routes.ReportSearchAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportSocialMediaOperatingMarginPage to ReportOnlineMarketplaceAlternativeChargePage when selectedActivities is OnlineMarketplace" in {

      navigator.nextPage(
        ReportSocialMediaOperatingMarginPage(periodKey),
        CheckMode,
        emptyUserAnswers
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.OnlineMarketplace))
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportSearchEngineOperatingMarginPage to ReportOnlineMarketplaceAlternativeChargePage when selectedActivities is OnlineMarketplace" in {

      navigator.nextPage(
        ReportSearchEngineOperatingMarginPage(periodKey),
        CheckMode,
        emptyUserAnswers
          .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.OnlineMarketplace))
          .success
          .value
      ) mustBe routes.ReportOnlineMarketplaceAlternativeChargeController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a ReportSearchEngineOperatingMarginPage to CheckYourAnswers page" in {

      navigator.nextPage(
        ReportSearchEngineOperatingMarginPage(periodKey),
        CheckMode,
        emptyUserAnswers
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from a GroupLiabilityPage to RepaymentPage" in {

      navigator.nextPage(
        GroupLiabilityPage(periodKey),
        CheckMode,
        UserAnswers("id")
      ) mustBe routes.RepaymentController.onPageLoad(periodKey, CheckMode)
    }

    "must go from a CompanyLiabilitiesPage to GroupLiability page when user filled the pages for both the companies mentioned in manage companies page" in {

      navigator.nextPage(
        CompanyLiabilitiesPage(periodKey, Index(1)),
        CheckMode,
        UserAnswers("id")
          .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
          .success
          .value
          .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
          .success
          .value
      ) mustBe routes.GroupLiabilityController.onPageLoad(periodKey, CheckMode)
    }

    "must go from Relief deducted page to Allowance deducted page" in {
      navigator.nextPage(
        ReliefDeductedPage(periodKey),
        CheckMode,
        UserAnswers("id").set(ReportAlternativeChargePage(periodKey), false).success.value
      ) mustBe routes.AllowanceDeductedController.onPageLoad(periodKey, CheckMode)
    }

    "must go from RepaymentPage to IsRepaymentBankAccountUKPage when user answers yes for repayment" in {
      navigator.nextPage(
        RepaymentPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(RepaymentPage(periodKey), true)
          .success
          .value
      ) mustBe routes.IsRepaymentBankAccountUKController.onPageLoad(periodKey, CheckMode)
    }

    "must go from RepaymentPage to CheckYourAnswersPage when user answers no for repayment" in {
      navigator.nextPage(
        RepaymentPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(RepaymentPage(periodKey), false)
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from IsRepaymentBankAccountUKPage to UKBankDetailsPage when bank account is from UK" in {
      navigator.nextPage(
        IsRepaymentBankAccountUKPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(IsRepaymentBankAccountUKPage(periodKey), true)
          .success
          .value
      ) mustBe routes.UKBankDetailsController.onPageLoad(periodKey, CheckMode)
    }

    "must go from UKBankDetailsPage to CheckYourAnswersPage" in {
      navigator.nextPage(
        UKBankDetailsPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            UKBankDetailsPage(periodKey),
            UKBankDetails(
              accountName = "AccountName",
              sortCode = "123456",
              accountNumber = "12345678",
              buildingNumber = Some("12345678")
            )
          )
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }

    "must go from IsRepaymentBankAccountUKPage to BankDetailsForRepaymentPage when bank account is Foreign" in {
      navigator.nextPage(
        IsRepaymentBankAccountUKPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(IsRepaymentBankAccountUKPage(periodKey), false)
          .success
          .value
      ) mustBe routes.BankDetailsForRepaymentController.onPageLoad(periodKey, CheckMode)
    }

    "must go from BankDetailsForRepaymentPage to CheckYourAnswersPage" in {
      navigator.nextPage(
        BankDetailsForRepaymentPage(periodKey),
        CheckMode,
        UserAnswers("id")
          .set(
            BankDetailsForRepaymentPage(periodKey),
            BankDetailsForRepayment(
              accountName = "AccountName",
              internationalBankAccountNumber = "GB36BARC20051773152391"
            )
          )
          .success
          .value
      ) mustBe routes.CheckYourAnswersController.onPageLoad(periodKey)
    }
  }
}
