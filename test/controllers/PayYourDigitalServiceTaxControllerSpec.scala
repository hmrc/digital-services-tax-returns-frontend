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

package controllers

import base.SpecBase
import connectors.DSTConnector
import models.registration.Period
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.PayYourDigitalServiceTaxView

import java.time.LocalDate
import scala.concurrent.Future

class PayYourDigitalServiceTaxControllerSpec extends SpecBase with MockitoSugar {

  val mockDstConnector: DSTConnector = mock[DSTConnector]

  "PayYourDigitalServiceTax Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[DSTConnector].toInstance(mockDstConnector)
        )
        .build()

      running(application) {
        val period =
          Period(LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), Period.Key("key"))

        when(mockDstConnector.lookupOutstandingReturns()(any())).thenReturn(Future.successful(Set(period)))

        val request = FakeRequest(GET, routes.PayYourDigitalServiceTaxController.onPageLoad().url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[PayYourDigitalServiceTaxView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(dstRegNumber, List(period).sortBy(_.start))(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
