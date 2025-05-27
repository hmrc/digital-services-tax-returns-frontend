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

import generators.ModelGenerators._
import models.registration._
import models.{CompanyName, DSTRegNumber}
import org.scalacheck.Arbitrary
import org.scalatest.OptionValues

import java.time.LocalDate

package object controllers extends OptionValues {
  private val regArbData: Registration = Arbitrary.arbitrary[Registration].sample.value
  private val address                  = Arbitrary.arbitrary[Address].sample.value

  val period: Period = Period(
    start = LocalDate.of(2020, 2, 28),
    end = LocalDate.of(2021, 2, 28),
    returnDue = LocalDate.of(2023, 2, 28),
    key = Period.Key("001")
  )

  val registration: Registration = regArbData.copy(
    registrationNumber = Some(DSTRegNumber("AMDST0799721562")),
    companyReg = CompanyRegWrapper(company = Company(CompanyName("Some Corporation"), address)),
    ultimateParent = None
  )
}
