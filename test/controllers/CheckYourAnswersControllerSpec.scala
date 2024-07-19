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
import generators.ModelGenerators.returnGen
import models.registration.Registration
import models.returns.Return
import models.{BankDetailsForRepayment, CompanyName, SelectActivities, UKBankDetails, formatDate}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary
import org.scalatestplus.mockito.MockitoSugar.mock
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ConversionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.CYAHelper
import views.html.{CheckYourAnswersView, TechnicalDifficultiesView}

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  val mockRegistration: Registration           = mock[Registration]
  val startDate: String                        = formatDate(period.start)
  val endDate: String                          = formatDate(period.end)
  val sectionList: Seq[SummaryListRow]         = Seq()
  val mockDSTConnector: DSTConnector           = mock[DSTConnector]
  val mockConversionService: ConversionService = mock[ConversionService]
  val displayName: CompanyName                 = CompanyName("Some Corporation")

  lazy val checkYourAnswersRoute: String = routes.CheckYourAnswersController.onPageLoad(periodKey).url

  "CheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(GroupLiabilityPage(periodKey), BigDecimal(40.00)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request     = FakeRequest(GET, checkYourAnswersRoute)
        val sectionList = new CYAHelper().createSectionList(periodKey, userAnswers)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, displayName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when all activities are selected" in {

      val userAnswers = emptyUserAnswers
        .set(GroupLiabilityPage(periodKey), BigDecimal(40.00))
        .success
        .value
        .set(SelectActivitiesPage(periodKey), SelectActivities.values.toSet)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request     = FakeRequest(GET, checkYourAnswersRoute)
        val sectionList = new CYAHelper().createSectionList(periodKey, userAnswers)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, displayName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when UK bank details are supplied" in {
      val accountName    = "AccountName"
      val sortCode       = "123456"
      val accountNumber  = "12345678"
      val buildingNumber = Some("1234567890")

      val userAnswers = emptyUserAnswers
        .set(IsRepaymentBankAccountUKPage(periodKey), true)
        .success
        .value
        .set(UKBankDetailsPage(periodKey), UKBankDetails(accountName, sortCode, accountNumber, buildingNumber))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request     = FakeRequest(GET, checkYourAnswersRoute)
        val sectionList = new CYAHelper().createSectionList(periodKey, userAnswers)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, displayName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when non-UK bank details are supplied" in {
      val accountName = "ForeignAccount"
      val iban        = "IBAN123456789"

      val userAnswers = emptyUserAnswers
        .set(IsRepaymentBankAccountUKPage(periodKey), false)
        .success
        .value
        .set(BankDetailsForRepaymentPage(periodKey), BankDetailsForRepayment(accountName, iban))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request     = FakeRequest(GET, checkYourAnswersRoute)
        val sectionList = new CYAHelper().createSectionList(periodKey, userAnswers)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, displayName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return Redirect to confirmation page on successful submission" in {

      val returnData = Arbitrary.arbitrary[Return].sample.value
      when(mockDSTConnector.submitReturn(any(), any())(any())).thenReturn(Future.successful(OK))
      when(mockConversionService.convertToReturn(any(), any())).thenReturn(Some(returnData))

      val userAnswers = emptyUserAnswers

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[DSTConnector].toInstance(mockDSTConnector),
            bind[ConversionService].toInstance(mockConversionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, checkYourAnswersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ReturnsCompleteController.onPageLoad(periodKey).url)
      }
    }

    "must return Redirect to 'technical difficulties' page on failed submission" in {

      val returnData = Arbitrary.arbitrary[Return].sample.value
      when(mockDSTConnector.submitReturn(any(), any())(any())).thenReturn(Future.successful(BAD_REQUEST))
      when(mockConversionService.convertToReturn(any(), any())).thenReturn(Some(returnData))

      val userAnswers = emptyUserAnswers

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[DSTConnector].toInstance(mockDSTConnector),
            bind[ConversionService].toInstance(mockConversionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, checkYourAnswersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TechnicalDifficultiesView]

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual view()(
          request,
          messages(application)
        ).toString
      }
    }

    "must return Redirect to 'JourneyRecovery' page when user has not completed the journey" in {

      when(mockConversionService.convertToReturn(any(), any())).thenReturn(None)

      val userAnswers = emptyUserAnswers

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[DSTConnector].toInstance(mockDSTConnector),
            bind[ConversionService].toInstance(mockConversionService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, checkYourAnswersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}
