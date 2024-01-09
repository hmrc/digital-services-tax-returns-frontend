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
import forms.ManageCompaniesFormProvider
import models.requests.DataRequest
import models.{Index, Mode, NormalMode, UserAnswers}
import navigation.Navigator
import pages.{CompanyDetailsListPage, ManageCompaniesPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.CompanyDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.ManageCompaniesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManageCompaniesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ManageCompaniesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ManageCompaniesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(periodKey: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CompanyDetailsListPage(periodKey)) match {
      case Some(list) if list.nonEmpty => Ok(view(form, periodKey, mode, getSummaryList(periodKey)))
      case _                           => Redirect(routes.CompanyDetailsController.onPageLoad(periodKey, Index(0), mode))
    }
  }

  private def getSummaryList(periodKey: String)(implicit request: DataRequest[AnyContent]): SummaryList = {
    val numberOfCompanies: Int              = request.userAnswers.get(CompanyDetailsListPage(periodKey)).fold(0)(_.size)
    val summaryListRow: Seq[SummaryListRow] = List.range(0, numberOfCompanies) flatMap { index =>
      CompanyDetailsSummary.row(periodKey, Index(index), request.userAnswers)
    }
    SummaryListViewModel(summaryListRow)
  }

  def onSubmit(periodKey: String, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, periodKey, mode, getSummaryList(periodKey)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ManageCompaniesPage(periodKey), value))
            } yield Redirect(navigator.nextPage(ManageCompaniesPage(periodKey), mode, updatedAnswers))
        )
  }

  def redirectToOnLoadPage(periodKey: String): Action[AnyContent] = (identify andThen getData) { implicit request =>
    request.userAnswers.getOrElse(UserAnswers(request.userId)).get(CompanyDetailsListPage(periodKey)) match {
      case Some(list) if list.nonEmpty => Redirect(routes.ManageCompaniesController.onPageLoad(periodKey, NormalMode))
      case _                           => Redirect(routes.CompanyDetailsController.onPageLoad(periodKey, Index(0), NormalMode))
    }
  }

}
