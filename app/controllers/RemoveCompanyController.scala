/*
 * Copyright 2026 HM Revenue & Customs
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

import controllers.actions.*
import forms.RemoveCompanyFormProvider
import models.requests.DataRequest
import models.{CompanyDetails, Index, Mode, PeriodKey}
import pages.{CompanyDetailsListPage, CompanyDetailsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveCompanyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCompanyController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  initialiseData: DataInitialiseAction,
  formProvider: RemoveCompanyFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCompanyView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(periodKey: PeriodKey, index: Index, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen initialiseData) { implicit request =>
      findCompanyDetails(periodKey, index, request) match {
        case Some(companyDetails) => Ok(view(form, periodKey, index, mode, companyDetails.companyName))
        case None                 => Redirect(routes.ManageCompaniesController.onPageLoad(periodKey, mode))
      }
    }

  def onSubmit(periodKey: PeriodKey, index: Index, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen initialiseData).async { implicit request =>
      findCompanyDetails(periodKey, index, request) match {
        case Some(companyDetails) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, periodKey, index, mode, companyDetails.companyName))),
              {
                case true  =>
                  for {
                    updatedAnswers <-
                      Future.fromTry(
                        request.userAnswers.remove(CompanyDetailsPage(periodKey, index))
                      )
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(routes.ManageCompaniesController.onPageLoad(periodKey, mode))
                case false => Future.successful(Redirect(routes.ManageCompaniesController.onPageLoad(periodKey, mode)))
              }
            )
        case None                 => Future.successful(Redirect(routes.ManageCompaniesController.onPageLoad(periodKey, mode)))
      }
    }

  private def findCompanyDetails(periodKey: PeriodKey, index: Index, request: DataRequest[AnyContent]) =
    request.userAnswers
      .get(CompanyDetailsListPage(periodKey))
      .flatMap(_.lift(index.position))

}
