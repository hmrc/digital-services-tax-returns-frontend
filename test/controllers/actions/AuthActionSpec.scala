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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DSTConnector
import controllers.routes
import generators.ModelGenerators._
import models.registration.Registration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with MockitoSugar {

  val mockDstConnector: DSTConnector = mock[DSTConnector]
  class Harness(authAction: IdentifierAction) {
    def onPageLoad() = authAction(_ => Results.Ok)
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new MissingBearerToken),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new BearerTokenExpired),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientEnrolments),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            mockDstConnector,
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }

    "The organization is registered for HMRC-DST-ORG enrollment" - {

      "must create an IdentifierRequest and allow the organisation to proceed when dstNewReturnsFrontendEnableFlag is true" in {

        type AuthRetrievals = Option[String]

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig    = mock[FrontendAppConfig]
          val registration = Arbitrary.arbitrary[Registration].sample.value

          val mockAuthConnector: AuthConnector = mock[AuthConnector]
          val retrieval: AuthRetrievals        = Some("Int-7e341-48319ddb53")

          when(appConfig.dstNewReturnsFrontendEnableFlag) thenReturn true

          when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
            retrieval
          )
          when(mockDstConnector.lookupRegistration()(any())).thenReturn(Future.successful(Some(registration)))

          val action     = new AuthenticatedIdentifierAction(mockAuthConnector, mockDstConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result     = controller.onPageLoad()(FakeRequest("", ""))
          status(result) mustBe OK

        }
      }

      "must return back to old frontend when dstNewReturnsFrontendEnableFlag is disabled" in {

        type AuthRetrievals = Option[String]

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers  = application.injector.instanceOf[BodyParsers.Default]
          val appConfig    = mock[FrontendAppConfig]
          val registration = Arbitrary.arbitrary[Registration].sample.value

          val mockAuthConnector: AuthConnector = mock[AuthConnector]
          val retrieval: AuthRetrievals        = Some("Int-7e341-48319ddb53")

          when(appConfig.dstNewReturnsFrontendEnableFlag) thenReturn false
          when(appConfig.dstFrontendRegistrationUrl) thenReturn "http://localhost:1234/oldReg"

          when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
            retrieval
          )
          when(mockDstConnector.lookupRegistration()(any())).thenReturn(Future.successful(Some(registration)))

          val action     = new AuthenticatedIdentifierAction(mockAuthConnector, mockDstConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result     = controller.onPageLoad()(FakeRequest("", ""))
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(appConfig.dstFrontendRegistrationUrl)

        }
      }

      "must return UnauthorizedException when internal Id is missing" in {

        type AuthRetrievals = Option[String]

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val mockAuthConnector: AuthConnector = mock[AuthConnector]
          val retrieval: AuthRetrievals        = None
          when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
            retrieval
          )

          val action     = new AuthenticatedIdentifierAction(mockAuthConnector, mockDstConnector, appConfig, bodyParsers)
          val controller = new Harness(action)

          val result = intercept[UnauthorizedException] {
            await(controller.onPageLoad()(FakeRequest("", "")))
          }
          result.message mustBe "Unable to retrieve internal Id"
        }
      }
    }

    "The organization is not registered for HMRC-DST-ORG enrollment" - {

      "must be redirected to registration journey" in {

        type AuthRetrievals = Option[String]

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val mockAuthConnector: AuthConnector = mock[AuthConnector]
          val retrieval: AuthRetrievals        = Some("Int-7e341-48319ddb53")
          when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
            retrieval
          )
          when(mockDstConnector.lookupRegistration()(any())).thenReturn(Future.successful(None))

          val action     = new AuthenticatedIdentifierAction(mockAuthConnector, mockDstConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result     = controller.onPageLoad()(FakeRequest("", ""))
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(appConfig.dstFrontendRegistrationUrl)

        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
