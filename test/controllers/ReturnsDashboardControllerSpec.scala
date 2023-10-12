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
import generators.ModelGenerators._
import models.registration.{Period, Registration}
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ReturnsDashboardView

class ReturnsDashboardControllerSpec extends SpecBase with ScalaCheckPropertyChecks {

  "ReturnsDashboard Controller" - {

    /* "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReturnsDashboardController.onPageLoad.url)
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnsDashboardView]

        status(result) mustEqual OK
        val registration = Arbitrary.arbitrary[Registration].sample.value
        val period = Arbitrary.arbitrary[Period].sample.value
        contentAsString(result) mustEqual view(registration, List(period), List(period))(request, messages(application),appConfig).toString
      }
    }*/
  }
}
