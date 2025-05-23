/*
 * Copyright 2025 HM Revenue & Customs
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
import models.registration.Period
import models.{PeriodKey, ResubmitAReturn, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.PreviousReturnsService
import views.html.ResubmitAReturnView

import java.time.Instant
import scala.concurrent.Future

class ResubmitAReturnControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call                                  = Call("GET", "/foo")
  val mockDstConnector: DSTConnector                     = mock[DSTConnector]
  val mockPreviousReturnsService: PreviousReturnsService = mock[PreviousReturnsService]
  val mockSessionRepository                              = mock[SessionRepository]
  val mockNavigator                                      = mock[Navigator]

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

      val userAnswers = new UserAnswers(id = "userId", data = Json.obj(), lastUpdated = Instant.now)

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockDstConnector.lookupAmendableReturns()(any())).thenReturn(Future.successful(Set.empty[Period]))
      when(mockPreviousReturnsService.convertReturnToUserAnswers(any[PeriodKey], any[UserAnswers])(any()))
        .thenReturn(Future.successful(Some(userAnswers)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[DSTConnector].toInstance(mockDstConnector),
            bind[PreviousReturnsService].toInstance(mockPreviousReturnsService)
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

    "onPageLoad must return NotFound when there are no amendable returns" in {

      val emptyUserAnswers = UserAnswers(id = "empty", data = Json.obj(), lastUpdated = Instant.now)

      when(mockDstConnector.lookupAmendableReturns()(any())).thenReturn(Future.successful(Set.empty[Period]))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[DSTConnector].toInstance(mockDstConnector),
          bind[PreviousReturnsService].toInstance(mockPreviousReturnsService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, resubmitAReturnRoute)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual "lookupAmendableReturns not found"
      }
    }

    "onSubmit must return NotFound when there are no amendable returns" in {

      val userAnswers = UserAnswers(id = "userId", data = Json.obj(), lastUpdated = Instant.now)

      when(mockDstConnector.lookupAmendableReturns()(any())).thenReturn(Future.successful(Set.empty[Period]))
      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[DSTConnector].toInstance(mockDstConnector),
          bind[PreviousReturnsService].toInstance(mockPreviousReturnsService),
          bind[Navigator].toInstance(mockNavigator)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, resubmitAReturnRoute)
          .withFormUrlEncodedBody(
            "field"  -> "value",
            "field2" -> "value2"
          )

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual "lookupAmendableReturns not found"
      }
    }
  }
}
