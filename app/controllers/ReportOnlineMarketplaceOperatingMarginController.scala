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
import forms.ReportOnlineMarketplaceOperatingMarginFormProvider
import javax.inject.Inject
import models.{Mode, PeriodKey}
import navigation.Navigator
import pages.ReportOnlineMarketplaceOperatingMarginPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReportOnlineMarketplaceOperatingMarginView

import scala.concurrent.{ExecutionContext, Future}

class ReportOnlineMarketplaceOperatingMarginController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ReportOnlineMarketplaceOperatingMarginFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReportOnlineMarketplaceOperatingMarginView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: PeriodKey, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData) { implicit request =>
      val groupOrCompany = request.registration.isGroupMessage

      val form         = formProvider(groupOrCompany)
      val preparedForm = request.userAnswers.get(ReportOnlineMarketplaceOperatingMarginPage(periodKey)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, periodKey, mode, groupOrCompany))
    }

  def onSubmit(periodKey: PeriodKey, mode: Mode): Action[AnyContent] =
    (identify(Some(periodKey)) andThen getData andThen requireData).async { implicit request =>
      val groupOrCompany = request.registration.isGroupMessage
      val form           = formProvider(groupOrCompany)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, periodKey, mode, groupOrCompany))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(ReportOnlineMarketplaceOperatingMarginPage(periodKey), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(ReportOnlineMarketplaceOperatingMarginPage(periodKey), mode, updatedAnswers)
            )
        )
    }
}
