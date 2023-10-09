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
import models.BackendAndFrontendJson._
import models.registration.{CompanyRegWrapper, Period, Registration}
import models.returns.Return
import models.{Postcode, UTR}
import org.scalacheck.Arbitrary.arbitrary
import org.scalactic.anyvals.PosInt
import play.api.libs.json.Json
import play.mvc.Http.Status
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.{ConfiguredPropertyChecks, WiremockServer}

import scala.concurrent.Future

class DSTConnectorSpec extends WiremockServer with ConfiguredPropertyChecks {

  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSize = 1, minSuccessful = PosInt(1))

  object DSTTestConnector extends DSTConnector(httpClient, servicesConfig) {
    override val backendURL: String = mockServerUrl
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "should lookup a company successfully" in {
    forAll { reg: CompanyRegWrapper =>
      stubFor(
        get(urlPathEqualTo(s"/lookup-company"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(reg).toString())
          )
      )

      whenReady(DSTTestConnector.lookupCompany()) { res =>
        res mustBe defined
        res.value mustEqual reg
      }
    }
  }

  "should lookup a company successfully by utr and postcode" in {
    forAll { (utr: UTR, postcode: Postcode, reg: CompanyRegWrapper) =>
      val escaped = postcode.replaceAll("\\s+", "")

      stubFor(
        get(urlPathEqualTo(s"/lookup-company/$utr/$escaped"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(reg).toString())
          )
      )

      val response = DSTTestConnector.lookupCompany(utr, postcode)
      whenReady(response) { res =>
        res mustBe defined
        res.value mustEqual reg
      }
    }
  }

  "should lookup a registration successfully" in {
    forAll { reg: Registration =>
      stubFor(
        get(urlPathEqualTo(s"/registration"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(reg).toString())
          )
      )

      val response = DSTTestConnector.lookupRegistration()
      whenReady(response) { res =>
        res mustBe defined
        res.value mustEqual reg
      }
    }
  }

  "should lookup a pending registration successfully" in {
    forAll { reg: Registration =>
      stubFor(
        get(urlPathEqualTo(s"/pending-registration"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson("DstRegNumber").toString())
          )
      )

      val response = DSTTestConnector.lookupPendingRegistrationExists()
      whenReady(response) { res =>
        res mustBe true
      }
    }
  }

  "should lookup a pending registration NotFound" in {
    forAll { reg: Registration =>
      stubFor(
        get(urlPathEqualTo(s"/pending-registration"))
          .willReturn(
            aResponse()
              .withStatus(404)
          )
      )

      val response = DSTTestConnector.lookupPendingRegistrationExists()
      whenReady(response) { res =>
        res mustBe false
      }
    }
  }

  "should lookup a list of outstanding return periods successfully" in {
    forAll { periods: Set[Period] =>
      stubFor(
        get(urlPathEqualTo(s"/returns/outstanding"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(periods).toString())
          )
      )

      val response = DSTTestConnector.lookupOutstandingReturns()
      whenReady(response) { res =>
        res must contain allElementsOf periods
      }
    }
  }

  "should lookup a list of submitted return periods successfully" in {
    forAll { periods: Set[Period] =>
      stubFor(
        get(urlPathEqualTo(s"/returns/amendable"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(periods).toString())
          )
      )

      val response = DSTTestConnector.lookupAmendableReturns()
      whenReady(response) { res =>
        res must contain allElementsOf periods
      }
    }
  }

  "should lookup a list of all return periods successfully" in {
    forAll { periods: Set[Period] =>
      stubFor(
        get(urlPathEqualTo(s"/returns/all"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(periods).toString())
          )
      )

      val response = DSTTestConnector.lookupAllReturns()
      whenReady(response) { res =>
        res must contain allElementsOf periods
      }
    }
  }
}
