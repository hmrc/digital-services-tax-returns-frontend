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

package connectors

import models.registration.{CompanyRegWrapper, Period, Registration}
import models.returns.Return
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import models.{UTR, Postcode}
import play.api.http.Status.OK

import scala.concurrent.{ExecutionContext, Future}

class DSTConnector(val http: HttpClient, servicesConfig: ServicesConfig)(implicit
                                                                         executionContext: ExecutionContext,
                                                                         hc: HeaderCarrier
) {

  val backendURL: String = servicesConfig.baseUrl("digital-services-tax") + "/digital-services-tax"

  def submitRegistration(reg: Registration): Future[HttpResponse] =
    http.POST[Registration, Either[UpstreamErrorResponse, HttpResponse]](s"$backendURL/registration", reg).map {
      case Right(value) => value
      case Left(e)      => throw e
    }

  def submitReturn(period: Period, ret: Return): Future[HttpResponse] = {
    val encodedKey = java.net.URLEncoder.encode(period.key, "UTF-8")
    http.POST[Return, Either[UpstreamErrorResponse, HttpResponse]](s"$backendURL/returns/$encodedKey", ret).map {
      case Right(value) => value
      case Left(e)      => throw e
    }
  }
  def lookupCompany(): Future[Option[CompanyRegWrapper]]              =
    http.GET[Option[CompanyRegWrapper]](s"$backendURL/lookup-company")

  def lookupCompany(utr: UTR, postcode: Postcode): Future[Option[CompanyRegWrapper]] =
    http.GET[Option[CompanyRegWrapper]](s"$backendURL/lookup-company/$utr/$postcode")

  def lookupRegistration(): Future[Option[Registration]] =
    http.GET[Option[Registration]](s"$backendURL/registration")

  def lookupPendingRegistrationExists(): Future[Boolean] =
    http.GET[HttpResponse](s"$backendURL/pending-registration").map {
      case resp if resp.status == OK => true
      case _                         => false
    }

  def lookupOutstandingReturns(): Future[Set[Period]] =
    http.GET[List[Period]](s"$backendURL/returns/outstanding").map(_.toSet)

  def lookupAmendableReturns(): Future[Set[Period]] =
    http.GET[List[Period]](s"$backendURL/returns/amendable").map(_.toSet)

  def lookupAllReturns(): Future[Set[Period]] =
    http.GET[List[Period]](s"$backendURL/returns/all").map(_.toSet)

  case class MicroServiceConnectionException(msg: String) extends Exception(msg)
}

