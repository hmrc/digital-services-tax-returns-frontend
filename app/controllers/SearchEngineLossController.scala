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

import controllers.actions._
import forms.SearchEngineLossFormProvider
import javax.inject.Inject
import models.{Mode, PeriodKey}
import navigation.Navigator
import pages.SearchEngineLossPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SearchEngineLossView

import scala.concurrent.{ExecutionContext, Future}

class SearchEngineLossController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SearchEngineLossFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SearchEngineLossView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: PeriodKey, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData) { implicit request =>
      val isGoupMessage = request.registration.isGroupMessage
      val form          = formProvider(isGoupMessage)
      val preparedForm  = request.userAnswers.get(SearchEngineLossPage(periodKey)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, periodKey, mode, isGoupMessage))
    }

  def onSubmit(periodKey: PeriodKey, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData).async { implicit request =>
      val isGroupMessage = request.registration.isGroupMessage
      val form           = formProvider(isGroupMessage)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, periodKey, mode, isGroupMessage))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SearchEngineLossPage(periodKey), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SearchEngineLossPage(periodKey), mode, updatedAnswers))
        )
    }
}
