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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import controllers.routes
import models.InternalId
import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.{GovernmentGateway, Verify}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, credentialRole, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.errors.{IncorrectAccountAffinity, IncorrectAccountCredRole}
import views.html.templates.Layout

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthorisedAction])
trait Auth
  extends ActionRefiner[Request, AuthorisedRequest]
    with ActionBuilder[AuthorisedRequest, AnyContent]
    with AuthorisedFunctions {
  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthorisedRequest[A]]]
}

@Singleton
class AuthorisedAction @Inject()(
                                  mcc: MessagesControllerComponents,
                                  layout: Layout,
                                  incorrectAccountAffinity: IncorrectAccountAffinity,
                                  incorrectAccountCredRole: IncorrectAccountCredRole,
                                  val authConnector: AuthConnector
                                )(implicit val appConfig: FrontendAppConfig, val executionContext: ExecutionContext, val messages: Messages)
  extends Auth {

  val logger = Logger(getClass)

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthorisedRequest[A]]] = {
    implicit val req: Request[A] = request
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val retrieval = allEnrolments and credentialRole and internalId and affinityGroup

    authorised(AuthProviders(GovernmentGateway, Verify) and Organisation and User).retrieve(retrieval) {
      case enrolments ~ _ ~ id ~ _ =>
        val internalIdString = id.getOrElse(throw new RuntimeException("No internal ID for user"))
        val internalId = InternalId
          .of(internalIdString)
          .getOrElse(
            throw new IllegalStateException("Invalid internal ID")
          )

        Future.successful(Right(AuthorisedRequest(internalId, enrolments, request)))

    } recover {
      case af: UnsupportedAffinityGroup =>
        logger.warn(s"invalid account affinity type, with message ${af.msg}, for reason ${af.reason}", af)
        Left(
          Ok(
            layout()(incorrectAccountAffinity())
          )
        )
      case ex: UnsupportedCredentialRole =>
        logger.warn(s"unsupported credential role on account, with message ${ex.msg}, for reason ${ex.reason}", ex)
        Left(
          Ok(
            layout(
            )(incorrectAccountCredRole())
          )
        )
      case _: NoActiveSession =>
        logger.info(s"Recover - no active session")
        Left(
          Redirect(routes.AuthenticationController.signIn())
        )
    }
  }

  override def parser = mcc.parsers.anyContent

}

case class AuthorisedRequest[A](
                                 internalId: InternalId,
                                 enrolments: Enrolments,
                                 request: Request[A]
                               ) extends WrappedRequest(request)
