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
import models.registration.Registration
import models.{BankDetailsForRepayment, SelectActivities, UKBankDetails, formatDate}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{BankDetailsForRepaymentPage, GroupLiabilityPage, IsRepaymentBankAccountUKPage, SelectActivitiesPage, UKBankDetailsPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.CYAHelper
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase {

  val mockRegistration: Registration   = mock[Registration]
  val startDate: String                = formatDate(period.start)
  val endDate: String                  = formatDate(period.end)
  val sectionList: Seq[SummaryListRow] = Seq()

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
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, registration)(
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
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, registration)(
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
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, registration)(
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
        contentAsString(result) mustEqual view(periodKey, sectionList, startDate, endDate, registration)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
