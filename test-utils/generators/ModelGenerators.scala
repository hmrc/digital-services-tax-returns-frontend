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

package generators

import cats.implicits.{none, _}
import models.registration._
import models.{AddressLine, CompanyName, CountryCode, DSTRegNumber, Email, NonEmptyString, PhoneNumber, Postcode, RegexValidatedString, RestrictiveString, SafeId, UTR}
import org.scalacheck.Arbitrary.{arbitrary, arbBigDecimal => _, _}
import org.scalacheck.cats.implicits._
import org.scalacheck.{Arbitrary, Gen}
import shapeless.tag.@@
import uk.gov.hmrc.auth.core._
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.LocalDate

object ModelGenerators {

  implicit def arbAddressLine: Arbitrary[AddressLine]           = Arbitrary(AddressLine.gen)
  implicit def arbPostcode: Arbitrary[Postcode]                 = Arbitrary(Postcode.gen.retryUntil(x => x == x.replaceAll(" ", "")))
  implicit def arbCountryCode: Arbitrary[CountryCode]           = Arbitrary(CountryCode.gen)
  implicit def arbCompanyName: Arbitrary[CompanyName]           = Arbitrary(CompanyName.gen)
  implicit def arbPhone: Arbitrary[PhoneNumber]                 = Arbitrary(PhoneNumber.gen)
  implicit def arbDSTNumber: Arbitrary[DSTRegNumber]            = Arbitrary(DSTRegNumber.gen)

  implicit class RichRegexValidatedString[A <: RegexValidatedString](val in: A) {
    def gen = RegexpGen.from(in.regex).map(in.apply)
  }

  implicit val argRestrictedString: Arbitrary[RestrictiveString] = Arbitrary(
    RestrictiveString.gen
//        RegexpGen.from("""^[0-9a-zA-Z{À-˿’}\\- &`'^._|]{1,255}$""")
  )
  implicit val arbString: Arbitrary[String] = Arbitrary(
    Gen.alphaNumStr.map(_.take(255))
  )

  def neString(maxLen: Int = 255) = (
    Gen.alphaNumChar,
    arbitrary[String]
    ).mapN((num, str) => s"$num$str").map(_.take(maxLen)).map(NonEmptyString.apply)

  // note this does NOT check all RFC-compliant email addresses (e.g. '"john doe"@company.co.uk')
  implicit def arbEmail: Arbitrary[Email] = Arbitrary {
    (
      neString(20),
      neString(20)
      ).mapN((a, b) => Email(s"$a@$b.co.uk"))
  }

  def date(start: LocalDate, end: LocalDate): Gen[LocalDate] =
    Gen.choose(start.toEpochDay, end.toEpochDay).map(LocalDate.ofEpochDay)

  implicit def arbAddr: Arbitrary[Address] = Arbitrary {

    val ukGen: Gen[Address] =
      (
        arbitrary[AddressLine],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[Postcode]
      ).mapN(UkAddress.apply)

    val foreignGen: Gen[Address] =
      (
        arbitrary[AddressLine],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[Option[AddressLine]],
        arbitrary[CountryCode]
      ).mapN(ForeignAddress.apply)

    Gen.oneOf(ukGen, foreignGen)

  }

  implicit def arbCo: Arbitrary[Company] = Arbitrary(
    (
      arbitrary[CompanyName],
      arbitrary[Address]
    ).mapN(Company.apply)
  )

  implicit def arbCoRegWrap: Arbitrary[CompanyRegWrapper] = Arbitrary(
    (
      arbitrary[Company],
      Gen.const(none[UTR]),
      Gen.const(none[SafeId]),
      Gen.const(false)
    ).mapN(CompanyRegWrapper.apply)
  )

  implicit def arbContact: Arbitrary[ContactDetails] = Arbitrary {
    (
      arbitrary[RestrictiveString],
      arbitrary[RestrictiveString],
      arbitrary[PhoneNumber],
      arbitrary[Email]
    ).mapN(ContactDetails.apply)
  }

  implicit def subGen: Arbitrary[Registration] = Arbitrary {
    (
      arbitrary[CompanyRegWrapper],
      Gen.option(arbitrary[Address]),
      arbitrary[Option[Company]],
      arbitrary[ContactDetails],
      date(LocalDate.of(2039, 1, 1), LocalDate.of(2040, 1, 1)),
      arbitrary[LocalDate],
      Gen.some(arbitrary[DSTRegNumber])
    ).mapN(Registration.apply)
  }

  implicit def arbCredRole: Arbitrary[CredentialRole] = Arbitrary {
    Gen.oneOf(List(User, Assistant))
  }

  implicit def arbAffinityGroup: Arbitrary[AffinityGroup] = Arbitrary {
    Gen.oneOf(List(AffinityGroup.Agent, AffinityGroup.Individual, AffinityGroup.Organisation))
  }

  implicit def periodKey: Arbitrary[@@[String, Period.Key.Tag]] = {
    val g = for {
      n <- Gen.chooseNum(1, 4)
      c <- Gen.alphaChar
      l <- Gen.listOfN(n, c)
    } yield Period.Key.apply(l.mkString)
    Arbitrary(g)
  }

  implicit def periodArb: Arbitrary[Period] =
    Arbitrary(
      (
        arbitrary[LocalDate],
        arbitrary[LocalDate],
        arbitrary[LocalDate],
        arbitrary[Period.Key]
        ).mapN(Period.apply)
    )

}
