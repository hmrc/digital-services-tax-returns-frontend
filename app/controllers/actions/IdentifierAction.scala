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
import uk.gov.hmrc.auth.core.AuthProvider.{GovernmentGateway, Verify}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
  override val authConnector: AuthConnector,
  dstConnector: DSTConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    authorised(AuthProviders(GovernmentGateway, Verify) and Organisation and User).retrieve(Retrievals.internalId) {
      case Some(internalId) =>
        if (config.dstNewReturnsFrontendEnableFlag) {
          lookupRegistration(request, internalId, block)
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
    internalId: String,
    block: IdentifierRequest[A] => Future[Result]
  )(implicit hc: HeaderCarrier): Future[Result] =
    dstConnector.lookupRegistration().flatMap {
      case Some(reg) if reg.registrationNumber.isDefined =>
        dstConnector.lookupAllReturns().flatMap { periods =>
          periods.toList match {
            case Nil     => Future.successful(NotFound)
            case periods =>
              val latest =
                periods.sortBy(_.start).head // TODO instead of head it should compare it with periodKey from url
              block(IdentifierRequest(request, internalId, reg, latest))
          }
        }
      case _                                             =>
        Future.successful(Redirect(config.dstFrontendRegistrationUrl))
    }

}
