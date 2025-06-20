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

package controllers.auth

import config.FrontendAppConfig
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.TimeOut

import javax.inject.Inject

class AuthenticationController @Inject() (mcc: MessagesControllerComponents, timeOutView: TimeOut)(implicit
  appConfig: FrontendAppConfig
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def signIn: Action[AnyContent] = Action {
    Redirect(
      url = appConfig.ggLoginUrl,
      queryStringParams = Map(
        "continue" -> Seq(controllers.routes.ReturnsDashboardController.onPageLoad.url),
        "origin"   -> Seq(appConfig.appName)
      )
    )
  }

  def signOut: Action[AnyContent] = Action {
    Redirect(appConfig.signOutDstUrl)
  }

  def timeOut: Action[AnyContent] = Action { implicit request =>
    Ok(timeOutView())
  }
}
