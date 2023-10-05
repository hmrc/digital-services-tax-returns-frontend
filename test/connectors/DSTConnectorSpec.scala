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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.registration.Registration
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, defined}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import utils.TestInstances.subGen
import utils.WiremockServer

class DSTConnectorSpec extends AnyFreeSpec with WiremockServer with GuiceOneAppPerSuite with ScalaFutures {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.digital-services-tax.port" -> mockServer.port()
    )
    .build()

  lazy val connector: DSTConnector = app.injector.instanceOf[DSTConnector]

  def stubPostResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    mockServer.stubFor(
      post(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  def stubGetResponse(url: String, status: Int, body: String = Json.obj().toString()): StubMapping =
    mockServer.stubFor(
      WireMock
        .get(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

  "DSTConnectorSpec" - {

    "lookupRegistration" - {
      "must successfully lookup a registration" in {
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
}
