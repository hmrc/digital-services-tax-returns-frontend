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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DSTConnector
import controllers.routes
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction {
  def apply(
    periodKey: Option[String] = None
  ): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]
}

class AuthIdentifierAction @Inject() (
  val authConnector: AuthConnector,
  dstConnector: DSTConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  override def apply(
    periodKey: Option[String] = None
  ): ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest] =
    new AuthenticatedIdentifierAction(authConnector, dstConnector, config, parser, periodKey)
}

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  dstConnector: DSTConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default,
  periodKey: Option[String]
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]
    with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authorised(AuthProviders(GovernmentGateway) and Organisation and User).retrieve(Retrievals.internalId) {
      case Some(internalId) =>
        if (config.dstNewReturnsFrontendEnableFlag) {
          lookupRegistration(request, periodKey, internalId, block)
        } else {
          Future.successful(Redirect(config.dstFrontendRegistrationUrl))
        }
      case _                => throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case _: NoActiveSession        =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  private def lookupRegistration[A](
    request: Request[A],
    periodKeyOpt: Option[String],
    internalId: String,
    block: IdentifierRequest[A] => Future[Result]
  )(implicit hc: HeaderCarrier): Future[Result] =
    dstConnector.lookupRegistration().flatMap {
      case Some(reg) if reg.registrationNumber.isDefined =>
        periodKeyOpt match {
          case Some(periodKey) =>
            dstConnector.lookupAllReturns().flatMap { periods =>
              periods.find(_.key == periodKey) match {
                case None         => Future.successful(NotFound)
                case Some(period) =>
                  block(IdentifierRequest(request, internalId, reg, Some(period)))
              }
            }
          case _               => block(IdentifierRequest(request, internalId, reg, None))
        }
      case _                                             =>
        Future.successful(Redirect(config.dstFrontendRegistrationUrl))
    }

}
