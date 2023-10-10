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
import models.BackendAndFrontendJson._
import models.registration.Registration
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, defined}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import utils.WiremockServer

class DSTConnectorSpec extends AnyFreeSpec with WiremockServer with GuiceOneAppPerSuite with ScalaFutures with IntegrationPatience {


  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.digital-services-tax.port" -> mockServer.port()
    )
    .build()

  lazy val connector: DSTConnector = app.injector.instanceOf[DSTConnector]

  "DSTConnectorSpec" - {

    "successfully lookup a registration" in {
      forAll { reg: Registration =>
        stubFor(
          get(urlPathEqualTo(s"/registration"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(Json.toJson(reg).toString())
            )
        )

        val response = connector.lookupRegistration()
        whenReady(response) { res =>
          res mustBe defined
          res.value mustEqual reg
        }
      }
    }
    }
}
