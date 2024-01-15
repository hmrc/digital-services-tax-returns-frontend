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

import connectors.DSTConnector
import controllers.actions._
import models.PeriodKey
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CYAHelper
import views.html.{CheckYourAnswersView, ReturnsCompleteView}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReturnsCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  dstConnector: DSTConnector,
  cyaHelper: CYAHelper,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnsCompleteView,
  cyaView: CheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(periodKey: PeriodKey): Action[AnyContent] = identify(Some(periodKey)).async { implicit request =>
    val submittedPeriodStart = request.submittedPeriodStart
    val submittedPeriodEnd   = request.submittedPeriodEnd
    val companyName          = request.registration.companyReg.company.name

    val sectionList = cyaHelper.createSectionList(request.userAnswers)

    val printableCYA: Option[Html] = Some(
      cyaView(
        periodKey,
        sectionList,
        submittedPeriodStart,
        submittedPeriodEnd,
        request.registration,
        isPrint = true,
        showBackLink = false
      )
    )

    for {
      outstandingPeriod <- dstConnector.lookupOutstandingReturns()
    } yield Ok(
      view(companyName, submittedPeriodStart, submittedPeriodEnd, outstandingPeriod.toList.minBy(_.start), printableCYA)
    )
  }
}
