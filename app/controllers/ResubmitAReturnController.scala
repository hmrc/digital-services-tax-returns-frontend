/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.DSTConnector
import controllers.actions._
import forms.ResubmitAReturnFormProvider
import models.{NormalMode, ResubmitAReturn}
import navigation.Navigator
import pages.ResubmitAReturnPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ResubmitAReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ResubmitAReturnController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  dstConnector: DSTConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  initialiseData: DataInitialiseAction,
  formProvider: ResubmitAReturnFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ResubmitAReturnView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[ResubmitAReturn] = formProvider()

  def onPageLoad: Action[AnyContent] = (identify() andThen getData).async { implicit request =>
    dstConnector.lookupAmendableReturns() map { outstandingPeriods =>
      outstandingPeriods.toList match {
        case Nil     =>
          NotFound("lookupAmendableReturns not found")
        case periods =>
          Ok(view(form, periods))
      }
    }
  }

  def onSubmit: Action[AnyContent] = (identify() andThen getData andThen initialiseData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          dstConnector.lookupAmendableReturns() map { outstandingPeriods =>
            outstandingPeriods.toList match {
              case Nil     =>
                NotFound("lookupAmendableReturns not found")
              case periods =>
                BadRequest(view(formWithErrors, periods))
            }
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ResubmitAReturnPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ResubmitAReturnPage, NormalMode, updatedAnswers))
      )
  }
}
