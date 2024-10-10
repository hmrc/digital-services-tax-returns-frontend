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
import models.requests.DataRequest
import models.{CompanyDetails, Index, Mode, PeriodKey}
import navigation.Navigator
import pages.{CompanyDetailsListPage, CompanyDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.CompanyDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CompanyDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  companyDetailsService: CompanyDetailsService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  initialiseData: DataInitialiseAction,
  formProvider: CompanyDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(periodKey: PeriodKey, index: Index, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen initialiseData) { implicit request =>
      val preparedForm = request.userAnswers.get(CompanyDetailsPage(periodKey, index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, periodKey, index, mode))
    }

  def onSubmit(periodKey: PeriodKey, index: Index, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen initialiseData).async { implicit request =>
      val boundForm = form.bindFromRequest()

      if (boundForm.hasErrors) {
        Future.successful(BadRequest(view(boundForm, periodKey, index, mode)))
      } else {
      val companyDetails: CompanyDetails = boundForm.value.head
        companyDetailsService
          .companyDetailsExists(request.userId, periodKey, companyDetails)
          .flatMap {
            case None => updateUserAnswersAndSession(request, periodKey, index, companyDetails, mode)
            case Some(true) =>
              Future.successful(
                BadRequest(view(boundForm.withError(formProvider.duplicateUtrFormError), periodKey, index, mode))
              )
            case Some(false) => updateUserAnswersAndSession(request, periodKey, index, companyDetails, mode)
          }
      }
    }

  def onDelete(periodKey: PeriodKey, index: Index, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <-
          Future.fromTry(
            request.userAnswers.remove(CompanyDetailsPage(periodKey, index))
          )
        _              <- sessionRepository.set(updatedAnswers)
      } yield {
        val size = updatedAnswers.get(CompanyDetailsListPage(periodKey)).fold(0)(_.size)
        size match {
          case 0 => Redirect(routes.CompanyDetailsController.onPageLoad(periodKey, Index(0), mode))
          case _ => Redirect(routes.ManageCompaniesController.onPageLoad(periodKey, mode))
        }
      }
    }

  private def updateUserAnswersAndSession(
    request: DataRequest[AnyContent],
    periodKey: PeriodKey,
    index: Index,
    companyDetails: CompanyDetails,
    mode: Mode
  ): Future[Result] =
    for {
      updatedAnswers <-
        Future.fromTry(request.userAnswers.set(CompanyDetailsPage(periodKey, index), companyDetails))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(CompanyDetailsPage(periodKey, index), mode, updatedAnswers))
}
