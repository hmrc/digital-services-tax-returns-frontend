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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import generators.ModelGenerators._
import models.PeriodKey
import models.SimpleJson._
import models.TestSampleData._
import models.registration.{Period, Registration}
import models.returns.Return
import org.scalacheck.Arbitrary
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{contain, convertToAnyMustWrapper, defined}
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import utils.WiremockServer

class DSTConnectorSpec extends AnyFreeSpec with WiremockServer with ScalaFutures with IntegrationPatience {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val emptyPeriods = Set.empty[Period]

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.digital-services-tax.port" -> mockServer.port()
    )
    .build()

  lazy val connector: DSTConnector = app.injector.instanceOf[DSTConnector]

  "DSTConnectorSpec" - {

    "successfully lookup a registration" in {
      val registration = Arbitrary.arbitrary[Registration].sample.value

      stubGet(Json.toJson(registration), s"/digital-services-tax/registration", OK)

      val response = connector.lookupRegistration()
      whenReady(response) { res =>
        res mustBe defined
        res.value mustEqual registration
      }
    }

    "successfully lookup a submitted return" in {
      implicit val arbitraryReturn: Arbitrary[Return] = Arbitrary(sampleReturn)
      val returns                                     = Arbitrary.arbitrary[Return].sample.value
      val key                                         = PeriodKey("003")

      stubGet(Json.toJson(returns), s"/digital-services-tax/returns/${key.value}", OK)

      val response = connector.lookupSubmittedReturns(key)
      whenReady(response) { res =>
        res mustBe defined
        res.value mustEqual returns
      }
    }

    "should return not found if no existing submitted return is found" in {
      val key = PeriodKey("XXX")
      stubGet(Json.toJson(sampleReturn), s"/digital-services-tax/returns/${key.value}", NOT_FOUND)

      val response = connector.lookupSubmittedReturns(key)
      whenReady(response) { res =>
        res mustBe None
      }
    }

    "should lookup a list of outstanding return periods successfully" in {
      val periods = Arbitrary.arbitrary[Set[Period]].sample.value

      stubGet(Json.toJson(periods), s"/digital-services-tax/returns/outstanding", OK)

      val response = connector.lookupOutstandingReturns()
      whenReady(response) { res =>
        res must contain allElementsOf periods
      }
    }

    "should return an empty Set of Period when no outstanding periods exist" in {

      stubGet(Json.toJson(emptyPeriods), s"/digital-services-tax/returns/outstanding", OK)

      val response = connector.lookupOutstandingReturns()
      whenReady(response) { res =>
        res mustBe emptyPeriods
      }
    }

    "should lookup a list of submitted return periods successfully" in {
      val periods = Arbitrary.arbitrary[Set[Period]].sample.value

      stubGet(Json.toJson(periods), s"/digital-services-tax/returns/amendable", OK)

      val response = connector.lookupAmendableReturns()
      whenReady(response) { res =>
        res must contain allElementsOf periods
      }
    }
  }

  "should return an empty Set of Period when there are no submitted return periods" in {

    stubGet(Json.toJson(emptyPeriods), s"/digital-services-tax/returns/amendable", OK)

    val response = connector.lookupAmendableReturns()
    whenReady(response) { res =>
      res mustBe emptyPeriods
    }
  }

  "should lookup a list of all return periods successfully" in {
    val periods = Arbitrary.arbitrary[Set[Period]].sample.value

    stubGet(Json.toJson(periods), s"/digital-services-tax/returns/all", OK)

    val response = connector.lookupAllReturns()
    whenReady(response) { res =>
      res must contain allElementsOf periods
    }
  }

  "should return an empty Set of Period when there are no return periods" in {
    val periods = Set.empty[Period]

    stubGet(Json.toJson(periods), s"/digital-services-tax/returns/all", OK)

    val response = connector.lookupAllReturns()
    whenReady(response) { res =>
      res mustBe periods
    }
  }

  private def stubGet(body: JsValue, url: String, status: Int): Any =
    mockServer.stubFor(
      get(urlPathEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body.toString())
        )
    )
}
