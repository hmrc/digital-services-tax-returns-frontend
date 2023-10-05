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

package controllers.services

import config.FrontendAppConfig
import connectors.DSTConnector
import models.registration.Registration
import play.api.Logger
import play.api.i18n.Messages

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRegistrations @Inject()(dstConnector: DSTConnector, pending: views.html.Pending, appConfig: FrontendAppConfig)(implicit
                                                                                                                          ec: ExecutionContext,
                                                                                                                          val messages: Messages
) {

  val logger = Logger(getClass)

  def isRegPendingOrRegNumExists: Future[(Boolean, Option[Registration])] = {

    dstConnector.lookupRegistration().flatMap {
      case None =>
        dstConnector.lookupPendingRegistrationExists().map((_, None))
      case Some(reg) if reg.registrationNumber.isDefined =>
        Future.successful((false, Some(reg)))
      case Some(_) =>
        Future.successful((true, None))
    }
  }

}
