/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.SpecBase
import connectors.DSTConnector
import models.Activity.{OnlineMarketplace, SearchEngine, SocialMedia}
import models._
import models.registration.GroupCompany
import models.returns.Return
import org.mockito.MockitoSugar.mock
import pages._
import uk.gov.hmrc.http.HeaderCarrier

import scala.collection.immutable.ListMap

class ConversionServiceSpec extends SpecBase {

  val mockDSTConnector: DSTConnector = mock[DSTConnector]
  implicit val hc: HeaderCarrier     = HeaderCarrier()
  val service                        = new ConversionService()

  "ConversionService" - {
    "must return None when userAnswers is empty" in {
      val userAnswers = UserAnswers("id")

      service.convertToReturn(periodKey, userAnswers) mustBe None
    }

    "must convert user Answers to Returns model" in {
      val percent = Percent(0.0.toFloat)

      val userAnswers = UserAnswers("id")
        .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
        .success
        .value
        .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
        .success
        .value
        .set(SelectActivitiesPage(periodKey), SelectActivities.values.toSet)
        .success
        .value
        .set(ReportAlternativeChargePage(periodKey), true)
        .success
        .value
        .set(SocialMediaLossPage(periodKey), true)
        .success
        .value
        .set(SearchEngineLossPage(periodKey), true)
        .success
        .value
        .set(ReportOnlineMarketplaceLossPage(periodKey), true)
        .success
        .value
        .set(RepaymentPage(periodKey), false)
        .success
        .value
        .set(RepaymentPage(periodKey), false)
        .success
        .value

      service.convertToReturn(periodKey, userAnswers) mustBe Some(
        Return(
          Set(SocialMedia, SearchEngine, OnlineMarketplace),
          Map(SocialMedia                               -> percent, SearchEngine -> percent, OnlineMarketplace -> percent),
          Money(0.0),
          None,
          ListMap(GroupCompany(CompanyName("C1"), None) -> Money(122.11)),
          Money(0.0),
          None
        )
      )
    }

    "must convert user Answers to Returns model when ReportAlternativeChargePage is false" in {
      val userAnswers = UserAnswers("id")
        .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
        .success
        .value
        .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
        .success
        .value
        .set(SelectActivitiesPage(periodKey), SelectActivities.values.toSet)
        .success
        .value
        .set(ReportAlternativeChargePage(periodKey), false)
        .success
        .value
        .set(RepaymentPage(periodKey), false)
        .success
        .value

      service.convertToReturn(periodKey, userAnswers) mustBe Some(
        Return(
          Set(SocialMedia, SearchEngine, OnlineMarketplace),
          Map.empty,
          Money(0.0),
          None,
          ListMap(GroupCompany(CompanyName("C1"), None) -> Money(122.11)),
          Money(0.0),
          None
        )
      )
    }

    "must convert user Answers to Returns model when selected activities have got the margin percentage" in {

      val userAnswers = UserAnswers("id")
        .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
        .success
        .value
        .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
        .success
        .value
        .set(SelectActivitiesPage(periodKey), SelectActivities.values.toSet)
        .success
        .value
        .set(ReportAlternativeChargePage(periodKey), true)
        .success
        .value
        .set(SocialMediaLossPage(periodKey), false)
        .success
        .value
        .set(ReportSocialMediaOperatingMarginPage(periodKey), 20.00)
        .success
        .value
        .set(SearchEngineLossPage(periodKey), false)
        .success
        .value
        .set(ReportSearchEngineOperatingMarginPage(periodKey), 30.00)
        .success
        .value
        .set(ReportOnlineMarketplaceLossPage(periodKey), false)
        .success
        .value
        .set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), 40.00)
        .success
        .value
        .set(RepaymentPage(periodKey), false)
        .success
        .value

      service.convertToReturn(periodKey, userAnswers) mustBe Some(
        Return(
          Set(SocialMedia, SearchEngine, OnlineMarketplace),
          Map(
            SocialMedia                                 -> Percent(20.00.toFloat),
            SearchEngine                                -> Percent(30.00.toFloat),
            OnlineMarketplace                           -> Percent(40.00.toFloat)
          ),
          Money(0.0),
          None,
          ListMap(GroupCompany(CompanyName("C1"), None) -> Money(122.11)),
          Money(0.0),
          None
        )
      )

    }

    "must convert user Answers to Returns model when 3 company details are provided" in {
      val userAnswers = UserAnswers("id")
        .set(SelectActivitiesPage(periodKey), SelectActivities.values.toSet)
        .success
        .value
        .set(ReportAlternativeChargePage(periodKey), false)
        .success
        .value
        .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
        .success
        .value
        .set(CompanyDetailsPage(periodKey, Index(1)), CompanyDetails("C2", None))
        .success
        .value
        .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
        .success
        .value
        .set(CompanyLiabilitiesPage(periodKey, Index(1)), BigDecimal(133.11))
        .success
        .value
        .set(RepaymentPage(periodKey), false)
        .success
        .value

      service.convertToReturn(periodKey, userAnswers) mustBe Some(
        Return(
          Set(SocialMedia, SearchEngine, OnlineMarketplace),
          Map.empty,
          Money(0.0),
          None,
          ListMap(
            GroupCompany(CompanyName("C1"), None) -> Money(122.11),
            GroupCompany(CompanyName("C2"), None) -> Money(133.11)
          ),
          Money(0.0),
          None
        )
      )

      service.convertToReturn(periodKey, userAnswers)
    }
  }

  "must convert user Answers to Returns model when non UK bank account details are provided" in {

    val bankDetails = BankDetailsForRepayment(
      accountName = "accountName",
      internationalBankAccountNumber = "GB36BARC20051773152391"
    )

    val userAnswers = UserAnswers("id")
      .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SocialMedia))
      .success
      .value
      .set(RepaymentPage(periodKey), true)
      .success
      .value
      .set(ReportAlternativeChargePage(periodKey), false)
      .success
      .value
      .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
      .success
      .value
      .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
      .success
      .value
      .set(IsRepaymentBankAccountUKPage(periodKey), false)
      .success
      .value
      .set(BankDetailsForRepaymentPage(periodKey), bankDetails)
      .success
      .value

    service.convertToReturn(periodKey, userAnswers) mustBe Some(
      Return(
        Set(SocialMedia),
        Map.empty,
        Money(0.0),
        None,
        ListMap(GroupCompany(CompanyName("C1"), None) -> Money(122.11)),
        Money(0.0),
        Some(RepaymentDetails(AccountName("accountName"), ForeignBankAccount(IBAN("GB36BARC20051773152391"))))
      )
    )

    service.convertToReturn(periodKey, userAnswers)
  }

  "must convert user Answers to Returns model when  UK bank account details are provided" in {

    val bankDetails = UKBankDetails(
      accountName = "accountName",
      sortCode = "123456",
      accountNumber = "12345678",
      buildingNumber = Some("buildingNumber")
    )

    val userAnswers = UserAnswers("id")
      .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
      .success
      .value
      .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
      .success
      .value
      .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SocialMedia))
      .success
      .value
      .set(RepaymentPage(periodKey), true)
      .success
      .value
      .set(ReportAlternativeChargePage(periodKey), false)
      .success
      .value
      .set(IsRepaymentBankAccountUKPage(periodKey), true)
      .success
      .value
      .set(UKBankDetailsPage(periodKey), bankDetails)
      .success
      .value

    service.convertToReturn(periodKey, userAnswers) mustBe Some(
      Return(
        Set(SocialMedia),
        Map.empty,
        Money(0.0),
        None,
        ListMap(GroupCompany(CompanyName("C1"), None) -> Money(122.11)),
        Money(0.0),
        Some(
          RepaymentDetails(
            AccountName("accountName"),
            DomesticBankAccount(
              SortCode("123456"),
              AccountNumber("12345678"),
              Some(BuildingSocietyRollNumber("buildingNumber"))
            )
          )
        )
      )
    )

    service.convertToReturn(periodKey, userAnswers)
  }

  "must convert user Answers to Returns model no bank details provided" in {

    val userAnswers = UserAnswers("id")
      .set(CompanyDetailsPage(periodKey, index), CompanyDetails("C1", None))
      .success
      .value
      .set(CompanyLiabilitiesPage(periodKey, index), BigDecimal(122.11))
      .success
      .value
      .set(SelectActivitiesPage(periodKey), Set[SelectActivities](SelectActivities.SocialMedia))
      .success
      .value
      .set(RepaymentPage(periodKey), false)
      .success
      .value
      .set(ReportAlternativeChargePage(periodKey), false)
      .success
      .value

    service.convertToReturn(periodKey, userAnswers) mustBe Some(
      Return(
        Set(SocialMedia),
        Map.empty,
        Money(0.0),
        None,
        ListMap(GroupCompany(CompanyName("C1"), None) -> Money(122.11)),
        Money(0.0),
        None
      )
    )

    service.convertToReturn(periodKey, userAnswers)
  }

}
