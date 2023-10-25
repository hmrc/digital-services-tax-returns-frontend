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
import forms.CompanyDetailsFormProvider
import models.requests.OptionalDataRequest
import models.{Index, Mode, UserAnswers}
import navigation.Navigator
import pages.{CompanyDetailsListPage, CompanyDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CompanyDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CompanyDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(index: Index, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = getUserAnswers(request).get(CompanyDetailsPage(index)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, index, mode))
  }

  private def getUserAnswers(implicit request: OptionalDataRequest[AnyContent]) =
    request.userAnswers.getOrElse(UserAnswers(request.userId))

  def onSubmit(index: Index, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode))),
        value =>
          for {
            updatedAnswers <-
              Future.fromTry(
                getUserAnswers(request).set(CompanyDetailsPage(index), value)
              )
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CompanyDetailsPage(index), mode, updatedAnswers))
      )
  }

  def onDelete(index: Index, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        updatedAnswers <-
          Future.fromTry(
            request.userAnswers.remove(CompanyDetailsPage(index))
          )
        _              <- sessionRepository.set(updatedAnswers)
      } yield {
        val size = updatedAnswers.get(CompanyDetailsListPage).fold(0)(_.size)
        size match {
          case 0 => Redirect(routes.CompanyDetailsController.onPageLoad(Index(0), mode))
          case _ => Redirect(routes.ManageCompaniesController.onPageLoad(mode))
        }
      }
  }
}
