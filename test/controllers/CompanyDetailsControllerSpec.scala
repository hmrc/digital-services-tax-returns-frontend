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
import forms.CompanyDetailsFormProvider
import models.{CompanyDetails, Index, NormalMode, PeriodKey, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CompanyDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.CompanyDetailsService
import views.html.CompanyDetailsView

import scala.concurrent.Future

class CompanyDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new CompanyDetailsFormProvider()
  val form         = formProvider()

  lazy val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(periodKey, index, NormalMode).url

  val userAnswers: UserAnswers =
    emptyUserAnswers
      .set(CompanyDetailsPage(periodKey, index), CompanyDetails("value 1", Some("1234567890")))
      .success
      .value

  "CompanyDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyDetailsRoute)

        val view = application.injector.instanceOf[CompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, periodKey, index, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when getData returns UserAnswers as 'None'" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, companyDetailsRoute)

        val view = application.injector.instanceOf[CompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, periodKey, index, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, companyDetailsRoute)

        val view = application.injector.instanceOf[CompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(CompanyDetails("value 1", Some("1234567890"))),
          periodKey,
          index,
          NormalMode
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val mockCompanyDetailsService = mock[CompanyDetailsService]

      when(mockCompanyDetailsService.companyDetailsExists("id", PeriodKey("003"), CompanyDetails("value 1", Some("1234567890"))))
        .thenReturn(Future.successful(None))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[CompanyDetailsService].toInstance(mockCompanyDetailsService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyDetailsRoute)
            .withFormUrlEncodedBody(("companyName", "value 1"), ("uniqueTaxpayerReference", "1234567890"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, companyDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, periodKey, index, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to company details page on removing the last company details from manage companies page" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CompanyDetailsController.onDelete(periodKey, index, NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CompanyDetailsController
          .onPageLoad(periodKey, index, NormalMode)
          .url
      }
    }

    "must redirect to company details page on when no company details records exists" in {
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
          FakeRequest(GET, routes.CompanyDetailsController.onDelete(periodKey, index, NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CompanyDetailsController
          .onPageLoad(periodKey, index, NormalMode)
          .url
      }
    }

    "must redirect to manage companies page on removing the one of the company name from the list of companies" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val ua = userAnswers
        .set(CompanyDetailsPage(periodKey, Index(1)), CompanyDetails("value 2", None))
        .success
        .value
        .set(CompanyDetailsPage(periodKey, Index(2)), CompanyDetails("value 3", None))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CompanyDetailsController.onDelete(periodKey, index, NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode).url
      }
    }

  }
}
