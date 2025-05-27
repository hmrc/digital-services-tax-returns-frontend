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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TimeOut

class AuthenticationControllerSpec extends SpecBase with MockitoSugar {

  "AuthenticationControllerSpec" - {

    "must sign the user in successfully" in {

      val application =
        applicationBuilder(None)
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.AuthenticationController.signIn().url)

        val result      = route(application, request).value
        val expectedUrl =
          s"${appConfig.ggLoginUrl}?continue=%2Fdigital-services-tax-returns&origin=digital-services-tax-returns-frontend"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedUrl
      }
    }

    "must sign the user out successfully" in {

      val application =
        applicationBuilder(None)
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.AuthenticationController.signOut.url)

        val result = route(application, request).value

        val expectedRedirectUrl = s"${appConfig.signOutDstUrl}"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }

    "timeout user out successfully" in {

      val application =
        applicationBuilder(None)
          .build()

      running(application) {
        val timeOutView = application.injector.instanceOf[TimeOut]
        val request     = FakeRequest(GET, routes.AuthenticationController.timeOut().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual timeOutView()(request, messages(application)).toString()

      }
    }
  }
}
