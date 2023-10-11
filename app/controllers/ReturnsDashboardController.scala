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

import config.FrontendAppConfig
import connectors.DSTConnector
import controllers.services.CheckRegistrations
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReturnsDashboardView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnsDashboardController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  val authConnector: AuthConnector,
  view: ReturnsDashboardView,
  dstConnector: DSTConnector,
  checkRegistrations: CheckRegistrations,
  pending: views.html.Pending
)(implicit
  ec: ExecutionContext,
  val appConfig: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    checkRegistrations.isRegPendingOrRegNumExists.flatMap {
      case (_, Some(reg)) =>
        for {
          outstandingPeriods <- dstConnector.lookupOutstandingReturns()
          amendedPeriods     <- dstConnector.lookupAmendableReturns()
        } yield Ok(
          view(reg, outstandingPeriods.toList.sortBy(_.start), amendedPeriods.toList.sortBy(_.start))
        )
      case (true, _)      =>
        logger.info("Registration is Pending")
        Future.successful(Ok(pending()))
      case _              =>
        Future.successful(Redirect(appConfig.dstFrontendBaseUrl))
    }
  }

}
