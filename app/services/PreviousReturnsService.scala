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
import models.{Activity, PeriodKey, UserAnswers}
import pages._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class PreviousReturnsService @Inject()(dstConnector: DSTConnector, userAnswers: UserAnswers)(implicit ec: ExecutionContext, hc: HeaderCarrier){

  def convertReturnToUserAnswers(periodKey: PeriodKey, userAnswers: UserAnswers): Future[Option[Try[UserAnswers]]] = {
    dstConnector.lookupSubmittedReturns(periodKey).map {
      case Some(returnData) =>
        val updatedUserAnswers = userAnswers.set(SelectActivitiesPage(periodKey), Activity.convert(returnData.reportedActivities))
          .flatMap(_.set(GroupLiabilityPage(periodKey), BigDecimal(returnData.totalLiability.toDouble)))
          .flatMap(_.set(CrossBorderTransactionReliefPage(periodKey), BigDecimal(returnData.crossBorderReliefAmount.toDouble)))
//          .flatMap(alternateChargeMap(periodKey, _, returnData))
        Some(updatedUserAnswers)
      case None => None
    }
  }

  def alternateChargeMap(periodKey: PeriodKey, userAnswers: UserAnswers, returnData: Return): UserAnswers = {

    returnData.alternateCharge.foldLeft(userAnswers)((ua, mapData) =>
      mapData._1 match {
        case SocialMedia => if (mapData._2 == 0) {
          ua.set(SocialMediaLossPage(periodKey), true).getOrElse(userAnswers)
        } else {
          ua.set(ReportSocialMediaOperatingMarginPage(periodKey), mapData._2.toDouble).getOrElse(userAnswers)
        }
        case SearchEngine => if (mapData._2 == 0) {
          ua.set(SearchEngineLossPage(periodKey), true).getOrElse(userAnswers)
        } else {
          ua.set(ReportSearchEngineOperatingMarginPage(periodKey), mapData._2.toDouble).getOrElse(userAnswers)
        }
        case OnlineMarketplace => if (mapData._2 == 0) {
          userAnswers.set(ReportOnlineMarketplaceLossPage(periodKey), true).getOrElse(userAnswers)
        } else {
          userAnswers.set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), mapData._2.toDouble).getOrElse(userAnswers)
        }
      }
    )
  }

}
