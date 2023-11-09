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
import generators.ModelGenerators._
import models.registration.{Address, Company, CompanyRegWrapper, Registration}
import models.{CompanyName, DSTRegNumber}
import org.scalacheck.Arbitrary
import org.scalatest.OptionValues

package object actions extends OptionValues {
  val registration: Registration = Arbitrary.arbitrary[Registration].sample.value
  val address                    = Arbitrary.arbitrary[Address].sample.value

  val updatedRegistration: Registration = registration.copy(
    registrationNumber = Some(DSTRegNumber("AMDST0799721562")),
    companyReg = CompanyRegWrapper(company = Company(CompanyName("Some Corporation"), address)),
    ultimateParent = None
  )
}
