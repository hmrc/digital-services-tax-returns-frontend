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

package models

import models.Activity.{OnlineMarketplace, SearchEngine, SocialMedia}
import models.registration.GroupCompany
import models.returns.Return
import scala.collection.immutable.ListMap

object TestSampleData {

  val sampleMoney: Money                             = Money(BigDecimal(100.00).setScale(2))
  val sampleActivitySet: Set[Activity]               = Set(SocialMedia, SearchEngine, OnlineMarketplace)
  val sampleDomesticBankAccount: DomesticBankAccount = DomesticBankAccount(
    SortCode("11-22-33"),
    AccountNumber("88888888"),
    Some(BuildingSocietyRollNumber("ABC - 123"))
  )
  val sampleRepaymentDetails: RepaymentDetails       = RepaymentDetails(
    AccountName("DigitalService"),
    sampleDomesticBankAccount
  )
  val samplePercent: Percent                         = Percent(50)

  val sampleCompanyName = CompanyName("TestCo Ltd")

  val sampleGroupCompany: GroupCompany = GroupCompany(
    sampleCompanyName,
    Some(UTR("1234567891"))
  )

  val sampleAlternativeCharge: Map[Activity, Percent] = Map(
    SocialMedia -> samplePercent
  )

  val sampleCompaniesAmount: ListMap[GroupCompany, Money] = ListMap(
    sampleGroupCompany -> sampleMoney
  )

  val sampleReturn: Return = Return(
    sampleActivitySet,
    sampleAlternativeCharge,
    sampleMoney,
    Some(sampleMoney),
    sampleCompaniesAmount,
    sampleMoney,
    Some(sampleRepaymentDetails)
  )

}
