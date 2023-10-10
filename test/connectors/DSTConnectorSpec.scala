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
import models.registration.CompanyRegWrapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.tomakehurst.wiremock.client.WireMock._
import test.utils.WiremockServer
import models.registration.Registration
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import app.models.BackendAndFrontendJson._
import config.FrontendAppConfig
import models.DSTRegNumber
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, defined}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, running}
import play.mvc.Http
import shapeless.tag.@@
import utils.WiremockServer
import app.models.registration.{Registration, CompanyRegWrapper, Address, Company, ContactDetails}


import java.time.LocalDate

class DSTConnectorSpec extends AnyFreeSpec with WiremockServer with GuiceOneAppPerSuite with ScalaFutures {


  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.digital-services-tax.port" -> mockServer.port()
    )
    .build()

  lazy val connector: DSTConnector = app.injector.instanceOf[DSTConnector]

  "DSTConnectorSpec" - {

    "successfully lookup a registration" in {

      val testRegistration = Registration(
        companyReg = CompanyRegWrapper("testID"),
        alternativeContact = Some(Address("123 Test Street", "Testville", "12345")),
        ultimateParent = Some(Company("Parent Co", Address("456 Parent St", "Parentville", "67890"))),
        contact = ContactDetails("Test", "Test", "test@example.com", "+1234567890"),
        dateLiable = LocalDate.of(2022, 1, 1),
        accountingPeriodEnd = LocalDate.of(2022, 12, 31),
        registrationNumber = DSTRegNumber("AADST1234567890")
      )

      // Stubbing the HTTP call
      stubFor(
        get(urlPathEqualTo("/registration"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(testRegistration).toString())
          )
      )

      // Calling the function under test
      val response = connector.lookupRegistration()

      // Verifying the result
      whenReady(response) { res =>
        res mustBe defined
        res.value mustEqual testRegistration
      }
    }
  }
}
