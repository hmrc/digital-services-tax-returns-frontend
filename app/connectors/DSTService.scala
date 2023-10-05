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

import cats.~>
import models._
import models.registration.{CompanyRegWrapper, Period, Registration}
import models.returns.Return
import uk.gov.hmrc.http.HttpResponse

trait DSTService[F[_]] {

  def lookupRegistration(): F[Option[Registration]]

  def lookupPendingRegistrationExists(): F[Boolean]

  def lookupOutstandingReturns(): F[Set[Period]]

  def lookupAmendableReturns(): F[Set[Period]]

  def lookupAllReturns(): F[Set[Period]]

  def transform[G[_]](nat: F ~> G): DSTService[G] = {
    val old = this
    new DSTService[G] {

      def lookupOutstandingReturns(): G[Set[Period]] = nat(old.lookupOutstandingReturns())

      def lookupRegistration(): G[Option[Registration]] = nat(old.lookupRegistration())

      def lookupPendingRegistrationExists(): G[Boolean] = nat(old.lookupPendingRegistrationExists())

      def lookupAmendableReturns(): G[Set[Period]] = nat(old.lookupAmendableReturns())

      def lookupAllReturns(): G[Set[Period]] = nat(old.lookupAllReturns())
    }
  }
}
