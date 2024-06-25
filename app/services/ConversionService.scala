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

import models.{Money, _}
import models.registration.GroupCompany
import models.returns.Return
import pages._
import shapeless.tag.@@

import scala.collection.immutable.ListMap

class ConversionService {

  def convertToReturn(periodKey: PeriodKey, userAnswers: UserAnswers): Option[Return] = {

    for {
      activities     <- userAnswers.get(SelectActivitiesPage(periodKey)).map(_.toSet)
      companyDetails <- userAnswers.get(CompanyDetailsListPage(periodKey))
      isRepaid <- userAnswers.get(RepaymentPage(periodKey))
    } yield {
      val alternateCharge: Map[Activity, Percent]        =
        if (userAnswers.get(ReportAlternativeChargePage(periodKey)).contains(true)) {
          alternateChargeMap(periodKey, userAnswers)
        } else { Map.empty }
      val totalLiability: BigDecimal @@ models.Money.Tag =
        userAnswers.get(GroupLiabilityPage(periodKey)).map(Money(_)).getOrElse(Money(BigDecimal(0.0)))
      val crossBorderReliefAmount                        =
        userAnswers.get(ReliefDeductedPage(periodKey)).map(Money(_)).getOrElse(Money(BigDecimal(0.0)))
      val allowanceAmount                                = userAnswers.get(AllowanceDeductedPage(periodKey)).map(Money(_))

      Return(
        reportedActivities = Activity.convertToActivity(activities),
        alternateCharge = alternateCharge,
        crossBorderReliefAmount = crossBorderReliefAmount,
        allowanceAmount = allowanceAmount,
        companiesAmount = companyMap(periodKey, companyDetails, userAnswers).to(ListMap),
        totalLiability = totalLiability,
        repayment = repayment(isRepaid, periodKey, userAnswers)
      )
    }

  }

  private def alternateChargeMap(
    periodKey: PeriodKey,
    userAnswers: UserAnswers
  ): Map[Activity, Percent] = {

    val map: Map[Activity, Percent] = Map.empty

    val map1: Map[Activity, Percent] = (
      userAnswers.get(SocialMediaLossPage(periodKey)),
      userAnswers.get(ReportSocialMediaOperatingMarginPage(periodKey))
    ) match {
      case (Some(true), None)          => map + (Activity.SocialMedia -> Percent(0.00.toFloat))
      case (Some(false), Some(margin)) => map + (Activity.SocialMedia -> Percent(margin.toFloat))
      case _                           => map
    }

    val map2: Map[Activity, Percent] = (
      userAnswers.get(SearchEngineLossPage(periodKey)),
      userAnswers.get(ReportSearchEngineOperatingMarginPage(periodKey))
    ) match {
      case (Some(true), None)          => map1 + (Activity.SearchEngine -> Percent(0.00.toFloat))
      case (Some(false), Some(margin)) => map1 + (Activity.SearchEngine -> Percent(margin.toFloat))
      case _                           => map1
    }

    val map3 = (
      userAnswers.get(ReportOnlineMarketplaceLossPage(periodKey)),
      userAnswers.get(ReportOnlineMarketplaceOperatingMarginPage(periodKey))
    ) match {
      case (Some(true), None)          => map2 + (Activity.OnlineMarketplace -> Percent(0.00.toFloat))
      case (Some(false), Some(margin)) => map2 + (Activity.OnlineMarketplace -> Percent(margin.toFloat))
      case _                           => map2
    }
    map3

  }

  private def companyMap(periodKey: PeriodKey, companyDetails: List[CompanyDetails], userAnswers: UserAnswers):
  Seq[(GroupCompany, Money)] =
    companyDetails.zipWithIndex.flatMap {
      case (companyDetail, index) =>
        val grpName   =
          GroupCompany(CompanyName(companyDetail.companyName), companyDetail.uniqueTaxpayerReference.map(UTR(_)))
       userAnswers.get(CompanyLiabilitiesPage(periodKey, Index(index))) map (liability => grpName -> Money(liability))
    }

  private def repayment(isRepaid: Boolean, periodKey: PeriodKey, userAnswers: UserAnswers): Option[RepaymentDetails] = {
    (isRepaid, userAnswers.get(IsRepaymentBankAccountUKPage(periodKey))) match {
      case (true, Some(true))  =>
        userAnswers.get(UKBankDetailsPage(periodKey)) map (bankDetails =>
          RepaymentDetails(
            AccountName(bankDetails.accountName),
            DomesticBankAccount(
              SortCode(bankDetails.sortCode),
              AccountNumber(bankDetails.accountNumber),
              bankDetails.buildingNumber.map(BuildingSocietyRollNumber(_))
            )
          )
        )
      case (true, Some(false)) =>
        userAnswers.get(BankDetailsForRepaymentPage(periodKey)) map (bankDetails =>
          RepaymentDetails(
            AccountName(bankDetails.accountName),
            ForeignBankAccount(
              IBAN(bankDetails.internationalBankAccountNumber)
            )
          )
        )
      case _           => None
    }
  }

}
