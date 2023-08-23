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

import scala.concurrent.Future

class CheckRegistrations {

  backend.lookupRegistration().flatMap {
    case None                                          =>
      backend.lookupPendingRegistrationExists().flatMap {
        case true =>
          logger.info("[PaymentsController] Pending registration")
          Future.successful(
            Ok(
              layout(
                pageTitle = Some(s"${msg("common.title.short")} - ${msg("common.title")}")
              )(views.html.end.pending()(msg))
            )
          )
        case _    => Future.successful(Redirect(routes.RegistrationController.registerAction(" ")))
      }
    case Some(reg) if reg.registrationNumber.isDefined =>
      backend.lookupOutstandingReturns().map { periods =>
        Ok(
          layout(
            pageTitle = Some(s"${msg("common.title.short")} - ${msg("common.title")}")
          )(payYourDst(reg.registrationNumber, periods.toList.sortBy(_.start))(msg))
        )
      }
    case Some(_)                                       =>
      Future.successful(
        Ok(
          layout(
            pageTitle = Some(s"${msg("common.title.short")} - ${msg("common.title")}")
          )(views.html.end.pending()(msg))
        )
      )
  }

}
