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
import config.FrontendAppConfig
import connectors.DSTConnector
import models.registration.Period
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ReturnsDashboardView

import java.time.LocalDate
import scala.concurrent.Future
import scala.language.postfixOps

class ReturnsDashboardControllerSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier     = HeaderCarrier()
  val mockDstConnector: DSTConnector = mock[DSTConnector]

  "ReturnsDashboard Controller" - {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[DSTConnector].toInstance(mockDstConnector)
        )
        .build()

      running(application) {
        val period1 =
          Period(LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), Period.Key("key"))

        val period2 =
          Period(LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), Period.Key("key"))

        when(mockDstConnector.lookupOutstandingReturns()(any())).thenReturn(Future.successful(Set(period1)))
        when(mockDstConnector.lookupAmendableReturns()(any())).thenReturn(Future.successful(Set(period2)))

        val request = FakeRequest(GET, routes.ReturnsDashboardController.onPageLoad.url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[ReturnsDashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          registration,
          List(period1)
            .sortBy(_.start),
          List(period2)
            .sortBy(_.start)
        )(request, messages(application)).toString
      }
    }
  }
}
