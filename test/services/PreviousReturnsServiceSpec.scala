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
import models.SimpleJson.returnFormat
import models.returns.Return
import models.{BankDetailsForRepayment, CompanyDetails, Index, SelectActivities, UKBankDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PreviousReturnsServiceSpec extends SpecBase with MockitoSugar with ScalaFutures with OptionValues with BeforeAndAfterEach {
  val mockDSTConnector: DSTConnector = mock[DSTConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val service = new PreviousReturnsService(dstConnector = mockDSTConnector)

  override def beforeEach(): Unit = {
    reset(mockDSTConnector)
    super.beforeEach()
  }

  "user reports online marketplace and social media activities with 0 margin" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
        |{
        |        "reportedActivities" : [
        |            "OnlineMarketplace",
        |            "SocialMedia"
        |        ],
        |        "alternateCharge" : {
        |            "OnlineMarketplace" : 0.0,
        |            "SocialMedia" : 0.0
        |        },
        |        "crossBorderReliefAmount" : 825482693855.0,
        |        "allowanceAmount" : 850679990317.0,
        |        "companiesAmount" : {},
        |        "totalLiability" : 17121745095.0
        |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(ReportOnlineMarketplaceLossPage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(SocialMediaLossPage(periodKey)) mustBe Some(true)
  }

  "user reports only SocialMedia activity" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {
                   |            "SocialMedia" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 825482693855.0,
                   |        "allowanceAmount" : 850679990317.0,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 17121745095.0
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(SocialMediaLossPage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(ReportMediaAlternativeChargePage(periodKey)) mustBe None
  }

  "user reports only SearchEngine activity" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "SearchEngine"
                   |        ],
                   |        "alternateCharge" : {
                   |            "SearchEngine" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 825482693855.0,
                   |        "allowanceAmount" : 850679990317.0,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 17121745095.0
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(SearchEngineLossPage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(ReportSearchAlternativeChargePage(periodKey)) mustBe None
  }

  "user reports only OnlineMarketplace activity" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace"
                   |        ],
                   |        "alternateCharge" : {
                   |            "OnlineMarketplace" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 825482693855.0,
                   |        "allowanceAmount" : 850679990317.0,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 17121745095.0
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(ReportOnlineMarketplaceLossPage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(ReportOnlineMarketplaceAlternativeChargePage(periodKey)) mustBe None
  }

  "OnlineMarketPlace and Social Media activities with positive margin" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace",
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {
                   |            "OnlineMarketplace" : 15.0,
                   |            "SocialMedia" : 20.0
                   |        },
                   |        "crossBorderReliefAmount" : 500000,
                   |        "allowanceAmount" : 600000,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 70000
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(ReportOnlineMarketplaceOperatingMarginPage(periodKey)) mustBe Some(15.0)
    updatedUserAnswers.get(ReportSocialMediaOperatingMarginPage(periodKey)) mustBe Some(20.0)
    updatedUserAnswers.get(ReportMediaAlternativeChargePage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(ReportAlternativeChargePage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(SocialMediaLossPage(periodKey)) mustBe None
  }

  "user reports OnlineMarketPlace activity with 0.0 margin and SocialMedia with no alternative charge" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace",
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {
                   |            "OnlineMarketplace" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 500000,
                   |        "allowanceAmount" : 600000,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 70000
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(ReportOnlineMarketplaceLossPage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(ReportSocialMediaOperatingMarginPage(periodKey)) mustBe None
    updatedUserAnswers.get(ReportAlternativeChargePage(periodKey)) mustBe Some(true)
    updatedUserAnswers.get(ReportMediaAlternativeChargePage(periodKey)) mustBe Some(false)
    updatedUserAnswers.get(SocialMediaLossPage(periodKey)) mustBe None
  }

  "alternative charge in returns data is empty" in {
    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace",
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {},
                   |        "crossBorderReliefAmount" : 500000,
                   |        "allowanceAmount" : 600000,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 70000
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(ReportAlternativeChargePage(periodKey)) mustBe Some(false)
  }

  "processing return data with cross-border relief amount" in {

    val crossBorderReliefAmount = 500000

    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace",
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {
                   |            "OnlineMarketplace" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 500000,
                   |        "allowanceAmount" : 600000,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 70000
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(CrossBorderTransactionReliefPage(periodKey)) mustBe Some(crossBorderReliefAmount)
  }

  "processing return data with allowance amount" in {

    val allowanceAmount = 1000
    val crossBorderReliefAmount = 500000
    val totalLiabilityAmount = 70000

    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace",
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {
                   |            "OnlineMarketplace" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 500000,
                   |        "allowanceAmount" : 1000,
                   |        "companiesAmount" : {},
                   |        "totalLiability" : 70000
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(AllowanceDeductedPage(periodKey)) mustBe Some(allowanceAmount)
    updatedUserAnswers.get(CrossBorderTransactionReliefPage(periodKey)) mustBe Some(crossBorderReliefAmount)
    updatedUserAnswers.get(GroupLiabilityPage(periodKey)) mustBe Some(totalLiabilityAmount)

  }

  "processing return data with domestic bank details" in {

    val accountName = "AccountName"
    val sortCode = "123456"
    val accountNumber = "12345678"
    val buildingNumber = None

    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |    "reportedActivities": [
                   |        "OnlineMarketplace",
                   |        "SocialMedia"
                   |    ],
                   |    "alternateCharge": {
                   |        "OnlineMarketplace": 0
                   |    },
                   |    "crossBorderReliefAmount": 500000,
                   |    "allowanceAmount": 1000,
                   |    "companiesAmount": {},
                   |    "totalLiability": 70000,
                   |    "repayment": {
                   |        "accountName": "AccountName",
                   |        "bankAccount": {
                   |            "sortCode": "123456",
                   |            "accountNo": "12345678",
                   |            "_type": "DomesticBankAccount"
                   |        }
                   |    }
                   |}""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value

    val bankDetails = UKBankDetails(
      accountName = accountName,
      sortCode = sortCode,
      accountNumber = accountNumber,
      buildingNumber = buildingNumber
    )
    updatedUserAnswers.get(UKBankDetailsPage(periodKey)) mustBe Some(bankDetails)
  }

  "processing return data with foreign bank details" in {

    val accountName = "AccountName"
    val iban = "FR2531682128768051490609537"

    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |    "reportedActivities": [
                   |        "OnlineMarketplace",
                   |        "SocialMedia"
                   |    ],
                   |    "alternateCharge": {
                   |        "OnlineMarketplace": 0
                   |    },
                   |    "crossBorderReliefAmount": 500000,
                   |    "allowanceAmount": 1000,
                   |    "companiesAmount": {},
                   |    "totalLiability": 70000,
                   |    "repayment": {
                   |        "accountName": "AccountName",
                   |        "bankAccount": {
                   |            "iban" : "FR2531682128768051490609537",
                   |            "_type": "ForeignBankAccount"
                   |        }
                   |    }
                   |}""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value

    val bankDetails = BankDetailsForRepayment(
      accountName = accountName,
      internationalBankAccountNumber = iban
    )
    updatedUserAnswers.get(BankDetailsForRepaymentPage(periodKey)) mustBe Some(bankDetails)
  }

  "processing return data with company amount" in {

    val companyName = "Company 1"
    val utr = Some("1111111000")
    val companyAmount = 1000.0
    val index = Index(0)

    val companyName2 = "Company 2"
    val utr2 = Some("2222222000")
    val companyAmount2 = 2000.0
    val index2 = Index(1)

    val userAnswers = emptyUserAnswers
      .set(
        SelectActivitiesPage(periodKey),
        Set[SelectActivities](SelectActivities.SocialMedia, SelectActivities.SearchEngine)
      ).success.value

    val returnData =
      Json.parse("""
                   |{
                   |        "reportedActivities" : [
                   |            "OnlineMarketplace",
                   |            "SocialMedia"
                   |        ],
                   |        "alternateCharge" : {
                   |            "OnlineMarketplace" : 0.0
                   |        },
                   |        "crossBorderReliefAmount" : 500000,
                   |        "allowanceAmount" : 1000,
                   |        "companiesAmount" : {
                   |                    "Company 1:1111111000" : 1000.0,
                   |                    "Company 2:2222222000" : 2000.0
                   |        },
                   |        "totalLiability" : 70000
                   |    }""".stripMargin).as[Return]

    when(mockDSTConnector.lookupSubmittedReturns(any())(any())).thenReturn(Future.successful(Some(returnData)))
    val updatedUserAnswers = service.convertReturnToUserAnswers(periodKey, userAnswers).futureValue.value
    updatedUserAnswers.get(CompanyDetailsPage(periodKey, index)).value mustBe CompanyDetails(companyName, utr)
    updatedUserAnswers.get(CompanyLiabilitiesPage(periodKey, index)).value mustBe companyAmount

    updatedUserAnswers.get(CompanyDetailsPage(periodKey, index2)).value mustBe CompanyDetails(companyName2, utr2)
    updatedUserAnswers.get(CompanyLiabilitiesPage(periodKey, index2)).value mustBe companyAmount2

  }

}
