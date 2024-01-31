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

package controllers

import base.SpecBase
import connectors.DSTConnector
import forms.ResubmitAReturnFormProvider
import models.ResubmitAReturn
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ResubmitAReturnView

import scala.concurrent.Future

class ResubmitAReturnControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call              = Call("GET", "/foo")
  val mockDstConnector: DSTConnector = mock[DSTConnector]

  lazy val resubmitAReturnRoute: String = routes.ResubmitAReturnController.onPageLoad.url

  val formProvider                = new ResubmitAReturnFormProvider()
  val form: Form[ResubmitAReturn] = formProvider()

  "ResubmitAReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[DSTConnector].toInstance(mockDstConnector)
        )
        .build()

      running(application) {
        when(mockDstConnector.lookupAmendableReturns()(any())).thenReturn(Future.successful(Set(period)))

        val request = FakeRequest(GET, resubmitAReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ResubmitAReturnView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, List(period))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, resubmitAReturnRoute)
            .withFormUrlEncodedBody(("value", period.key))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[DSTConnector].toInstance(mockDstConnector)
        )
        .build()

      when(mockDstConnector.lookupAmendableReturns()(any())).thenReturn(Future.successful(Set(period)))

      running(application) {
        val request =
          FakeRequest(POST, resubmitAReturnRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ResubmitAReturnView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, List(period))(request, messages(application)).toString
      }
    }

  }
}
