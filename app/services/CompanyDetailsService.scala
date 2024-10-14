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

package services

import models.{CompanyDetails, PeriodKey}
import pages.companyDetailsList
import repositories.SessionRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyDetailsService @Inject() (sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {

  def companyDetailsExists(
    userId: String,
    periodKey: PeriodKey,
    companyDetails: CompanyDetails
  ): Future[Option[Boolean]] =
    sessionRepository
      .get(userId)
      .map {
        _.map { userAnswers =>
          userAnswers
            .findByAttr[List[CompanyDetails]](periodKey, companyDetailsList)
            .exists(companiesDetails =>
              companiesDetails
                .contains(CompanyDetails(companyDetails.companyName, companyDetails.uniqueTaxpayerReference))
            )
        }
      }
}
