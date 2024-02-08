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
import models.SelectActivities
import models.SimpleJson.returnFormat
import models.returns.Return
import org.scalatestplus.mockito.MockitoSugar
import pages.{ReportOnlineMarketplaceLossPage, SelectActivitiesPage, SocialMediaLossPage}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class PreviousReturnsServiceSpec extends SpecBase with MockitoSugar {
  val mockDSTConnector: DSTConnector = mock[DSTConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val service = new PreviousReturnsService(dstConnector = mockDSTConnector, userAnswers = emptyUserAnswers)

  "test" in {
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

    service.alternateChargeMap(periodKey, userAnswers, returnData) mustBe userAnswers
      .set(ReportOnlineMarketplaceLossPage(periodKey), true).success.value
      .set(SocialMediaLossPage(periodKey), true).success.value
  }


}
