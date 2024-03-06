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

import connectors.DSTConnector
import models.Activity.{OnlineMarketplace, SearchEngine, SocialMedia}
import models.returns.Return
import models.{Activity, BankDetailsForRepayment, CompanyDetails, DomesticBankAccount, ForeignBankAccount, Index, Money, PeriodKey, UKBankDetails, UserAnswers}
import pages._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class PreviousReturnsService @Inject() (dstConnector: DSTConnector)(implicit ec: ExecutionContext) {

  def convertReturnToUserAnswers(periodKey: PeriodKey, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier
  ): Future[Option[UserAnswers]] =
    dstConnector.lookupSubmittedReturns(periodKey).map {
      case Some(returnData) =>
        val updatedUserAnswers = userAnswers
          .set(SelectActivitiesPage(periodKey), Activity.convert(returnData.reportedActivities))
          .flatMap(_.set(GroupLiabilityPage(periodKey), BigDecimal(returnData.totalLiability.toDouble)))
          .flatMap(_.set(ReportCrossBorderReliefPage(periodKey), returnData.crossBorderReliefAmount.toDouble > 0))
          .flatMap(
            _.set(ReliefDeductedPage(periodKey), BigDecimal(returnData.crossBorderReliefAmount.toDouble))
          )
          .flatMap(_.set(ReportAlternativeChargePage(periodKey), returnData.alternateCharge.nonEmpty))
          .flatMap(ua => alternateChargeMap(periodKey, ua, returnData))
          .flatMap(_.set(AllowanceDeductedPage(periodKey), returnData.allowanceAmount.getOrElse(Money(0.0))))
          .flatMap(ua => companyMap(periodKey, ua, returnData))
          .flatMap(_.set(RepaymentPage(periodKey), returnData.repayment.isDefined))
          .flatMap { ua =>
            returnData.repayment match {
              case Some(repaymentDetails) =>
                repaymentDetails.bankAccount match {
                  case DomesticBankAccount(sortCode, accountNo, _) =>
                    val domesticBankDetails = UKBankDetails(repaymentDetails.accountName, sortCode, accountNo, None)
                    ua.set(IsRepaymentBankAccountUKPage(periodKey), true)
                      .flatMap(_.set(UKBankDetailsPage(periodKey), domesticBankDetails))
                  case ForeignBankAccount(iban)                    =>
                    val foreignBankDetails = BankDetailsForRepayment(repaymentDetails.accountName, iban)
                    ua.set(IsRepaymentBankAccountUKPage(periodKey), false)
                    .flatMap(_.set(BankDetailsForRepaymentPage(periodKey), foreignBankDetails))
                }
              case None                   => Try(ua)
            }
          }
        updatedUserAnswers.toOption
      case None             => None
    }

  private def alternateChargeMap(
    periodKey: PeriodKey,
    userAnswers: UserAnswers,
    returnData: Return
  ): Try[UserAnswers] = {

    val a = returnData.alternateCharge.foldLeft(userAnswers)((ua, mapData) =>
      mapData._1 match {
        case SocialMedia       =>
          if (mapData._2 == 0) {
            ua.set(SocialMediaLossPage(periodKey), true).getOrElse(userAnswers)
          } else {
            ua.set(ReportSocialMediaOperatingMarginPage(periodKey), mapData._2.toDouble).getOrElse(userAnswers)
          }
        case SearchEngine      =>
          if (mapData._2 == 0) {
            ua.set(SearchEngineLossPage(periodKey), true).getOrElse(userAnswers)
          } else {
            ua.set(ReportSearchEngineOperatingMarginPage(periodKey), mapData._2.toDouble).getOrElse(userAnswers)
          }
        case OnlineMarketplace =>
          if (mapData._2 == 0) {
            ua.set(ReportOnlineMarketplaceLossPage(periodKey), true).getOrElse(userAnswers)
          } else {
            ua.set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), mapData._2.toDouble).getOrElse(userAnswers)
          }
      }
    )
    if (a.get(SelectActivitiesPage(periodKey)).exists(_.size > 1)) {
      a.set(
        ReportOnlineMarketplaceAlternativeChargePage(periodKey),
        returnData.alternateCharge.contains(OnlineMarketplace)
      ).flatMap(_.set(ReportSearchAlternativeChargePage(periodKey), returnData.alternateCharge.contains(SearchEngine)))
        .flatMap(_.set(ReportMediaAlternativeChargePage(periodKey), returnData.alternateCharge.contains(SocialMedia)))
    } else {
      Try(a)
    }
  }

  private def companyMap(periodKey: PeriodKey, userAnswers: UserAnswers, returnData: Return): Try[UserAnswers] =
    returnData.companiesAmount.zipWithIndex.foldLeft(Try(userAnswers)) { (ua, data) =>
      val (company, amount) = data._1
      val index             = data._2
      ua.flatMap { ua =>
        ua.set(CompanyLiabilitiesPage(periodKey, Index(index)), amount)
          .flatMap(_.set(CompanyDetailsPage(periodKey, Index(index)), CompanyDetails(company.name, company.utr)))
      }
    }

}
