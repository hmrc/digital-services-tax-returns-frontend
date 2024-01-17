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
import models.formatDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.CYAHelper
import viewmodels.govuk.SummaryListFluency
import views.html.{CheckYourAnswersView, ReturnsCompleteView}

import scala.concurrent.Future

class ReturnsCompleteControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {
  implicit val hc: HeaderCarrier     = HeaderCarrier()
  val mockDstConnector: DSTConnector = mock[DSTConnector]
  val companyName                    = registration.companyReg.company.name
  val startDate                      = formatDate(period.start)
  val endDate                        = formatDate(period.end)

  "ReturnsComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(inject.bind[DSTConnector].toInstance(mockDstConnector))
        .build()

      running(application) {

        when(mockDstConnector.lookupOutstandingReturns()(any())).thenReturn(Future.successful(Set(period)))

        val request     = FakeRequest(GET, routes.ReturnsCompleteController.onPageLoad(periodKey).url)
        val sectionList = new CYAHelper().createSectionList(periodKey, emptyUserAnswers)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnsCompleteView]
        val cya  = application.injector.instanceOf[CheckYourAnswersView]

        val printableCYA = Some(
          cya(periodKey, sectionList, startDate, endDate, registration, isPrint = true, showBackLink = false)(
            request,
            messages(application)
          )
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(companyName, startDate, endDate, period, printableCYA)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
