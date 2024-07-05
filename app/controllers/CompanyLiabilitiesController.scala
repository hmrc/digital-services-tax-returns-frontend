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
import forms.CompanyLiabilitiesFormProvider
import models.{Index, Mode, PeriodKey}
import navigation.Navigator
import pages.{CompanyDetailsPage, CompanyLiabilitiesPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CompanyLiabilitiesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompanyLiabilitiesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CompanyLiabilitiesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CompanyLiabilitiesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(periodKey: PeriodKey, mode: Mode, index: Index): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(CompanyDetailsPage(periodKey, index)) match {
        case Some(companyDetails) =>
          val startDate = request.periodStartDate
          val endDate   = request.periodEndDate

          val form         = formProvider(companyDetails.companyName)
          val preparedForm = request.userAnswers.get(CompanyLiabilitiesPage(periodKey, index)) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, periodKey, mode, index, companyDetails.companyName, startDate, endDate))
        case _                    =>
          logger.logger.info("CompanyDetailsPage is missing data")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }

    }

  def onSubmit(periodKey: PeriodKey, mode: Mode, index: Index): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(CompanyDetailsPage(periodKey, index)) match {
        case Some(companyDetails) =>
          val companyName = companyDetails.companyName
          val startDate   = request.periodStartDate
          val endDate     = request.periodEndDate
          val form        = formProvider(companyName)

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, periodKey, mode, index, companyName, startDate, endDate))
                ),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(CompanyLiabilitiesPage(periodKey, index), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield {
                  Redirect(navigator.nextPage(CompanyLiabilitiesPage(periodKey, index), mode, updatedAnswers))
                }
            )
        case _                    =>
          logger.logger.info("CompanyDetailsPage is missing data")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
