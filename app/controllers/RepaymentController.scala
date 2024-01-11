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
import forms.RepaymentFormProvider

import javax.inject.Inject
import models.{Mode, formatDate}
import navigation.Navigator
import pages.RepaymentPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RepaymentView

import scala.concurrent.{ExecutionContext, Future}

class RepaymentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RepaymentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RepaymentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val startDate = formatDate(request.period.start)
      val endDate   = formatDate(request.period.end)

      val form         = formProvider(startDate, endDate)
      val preparedForm = request.userAnswers.get(RepaymentPage(periodKey)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, periodKey, mode, startDate, endDate))
  }

  def onSubmit(periodKey: String, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val startDate = formatDate(request.period.start)
      val endDate   = formatDate(request.period.end)
      val form      = formProvider(startDate, endDate)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, periodKey, mode, startDate, endDate))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(RepaymentPage(periodKey), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RepaymentPage(periodKey), mode, updatedAnswers))
        )
    }
}
