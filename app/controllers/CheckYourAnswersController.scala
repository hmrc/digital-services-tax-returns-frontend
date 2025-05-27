/*
 * Copyright 2025 HM Revenue & Customs
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

import com.google.inject.Inject
import connectors.DSTConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{CompanyName, PeriodKey}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ConversionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CYAHelper
import viewmodels.Section
import views.html.{CheckYourAnswersView, TechnicalDifficultiesView}

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  conversionService: ConversionService,
  dstConnector: DSTConnector,
  cyaHelper: CYAHelper,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  errorView: TechnicalDifficultiesView
)(implicit ex: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: PeriodKey): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData) { implicit request =>
      val startDate: String         = request.periodStartDate
      val endDate: String           = request.periodEndDate
      val sectionList: Seq[Section] = cyaHelper.createSectionList(periodKey, request.userAnswers)
      val displayName: CompanyName  =
        request.registration.ultimateParent.fold(request.registration.companyReg.company.name)(_.name)

      Ok(view(periodKey, sectionList, startDate, endDate, displayName))
    }

  def onSubmit(periodKey: PeriodKey): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData).async { implicit request =>
      conversionService.convertToReturn(periodKey, request.userAnswers) match {
        case Some(returnData) =>
          request.period match {
            case Some(period) =>
              dstConnector.submitReturn(period = period, returnData) flatMap {
                case OK => Future.successful(Redirect(routes.ReturnsCompleteController.onPageLoad(periodKey)))
                case _  =>
                  Future.successful(InternalServerError(errorView()))
              }
            case _            => Future.successful(NotFound)
          }
        case _                => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
