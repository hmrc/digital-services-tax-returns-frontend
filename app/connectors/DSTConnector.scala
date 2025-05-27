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

package connectors

import models.PeriodKey
import models.SimpleJson._
import models.registration.{Period, Registration}
import models.returns.Return
import play.api.Logging
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DSTConnector @Inject() (http: HttpClient, servicesConfig: ServicesConfig)(implicit
  executionContext: ExecutionContext
) extends Logging {

  private val backendURL: String = servicesConfig.baseUrl("digital-services-tax") + "/digital-services-tax"

  def lookupRegistration()(implicit hc: HeaderCarrier): Future[Option[Registration]] =
    http.GET[Option[Registration]](s"$backendURL/registration")

  def lookupOutstandingReturns()(implicit hc: HeaderCarrier): Future[Set[Period]] =
    http.GET[List[Period]](s"$backendURL/returns/outstanding").map(_.toSet)

  def lookupAmendableReturns()(implicit hc: HeaderCarrier): Future[Set[Period]] =
    http.GET[List[Period]](s"$backendURL/returns/amendable").map(_.toSet)

  def lookupAllReturns()(implicit hc: HeaderCarrier): Future[Set[Period]] =
    http.GET[List[Period]](s"$backendURL/returns/all").map(_.toSet)

  def lookupSubmittedReturns(periodKey: PeriodKey)(implicit hc: HeaderCarrier): Future[Option[Return]] =
    http.GET[Option[Return]](s"$backendURL/returns/${periodKey.value}")

  def submitReturn(period: Period, ret: Return)(implicit hc: HeaderCarrier): Future[Int] = {
    val encodedKey = java.net.URLEncoder.encode(period.key, "UTF-8")
    http.POST[Return, HttpResponse](s"$backendURL/returns/$encodedKey", ret) map { response =>
      response.status
    }
  } recoverWith { case e: Exception =>
    logger.warn(s"An Exception thrown from downstream service: ${e.getMessage}")
    Future.successful(INTERNAL_SERVER_ERROR)
  }
}
