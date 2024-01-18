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

package models

import cats.implicits._
import cats.kernel.Monoid
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.anyvals.PosInt
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.ConfiguredPropertyChecks

import java.time.LocalDate

class ValidatedTypeSpec extends AnyFlatSpec with Matchers with ConfiguredPropertyChecks {
  val ibanList = List(
    "AD9179714843548170724658",
    "AE532299249995935421750",
    "AL36442788709271283994894168",
    "AT836670643070032585",
    "AZ73GIGY95609694664952978687",
    "BA590483797190961278",
    "BE83158799706920",
    "BG29EFYA04741506522965",
    "BH45BSND64749329633519",
    "BR2900994781520950149594104E9",
    "BY13NBRB3600900000002Z00AB00",
    "CH2669331784946419826",
    "CR05015202001026284066",
    "CY75371695368399798483913159",
    "CZ9667197451460125048740",
    "DE09355072686373974067",
    "DK2017303660295651",
    "DO72758351294497410197174992",
    "EE803801541625441184",
    "ES2837832292261368335005",
    "FI6346426364591804",
    "FO5778020271560713",
    "FR2531682128768051490609537",
    "GB42RHBR54612317078692",
    "GE20NB4430283848566197",
    "GI44UQCR057428239600992",
    "GL0502210301477863",
    "GR7642039964850941669463374",
    "GT46866930583377769763582334",
    "HR7971488917057910183",
    "HU91584374853132521152169392",
    "IE27CTJG26368089744633",
    "IL862354629185675963157",
    "IQ98NBIQ850123456789012",
    "IS925454646268060577432601",
    "IT82K7579579031831283849724",
    "JO94CBJO0010000000000131000302",
    "KW23YOUF8981762754308793114869",
    "KZ830907614112967961",
    "LB52372840451692007088329912",
    "LC55HEMM000100010012001200023015",
    "LI1935676368061346475",
    "LT192412904756279442",
    "LU951073196477746242",
    "LV88HAGO9615590045208",
    "MC3212096543766737867569392",
    "MD9881991481700015080266",
    "ME36752723963983139414",
    "MK31948483764450559",
    "MR5442207381102036325668909",
    "MT84AIWA00813109843252965695890",
    "MU65OSUR4348845783494200142ABC",
    "NL64VCYA2607250706",
    "NO2451742753161",
    "PK13LCJY3078553597017331",
    "PL64778828144458067930515057",
    "PS38LEKA813957891201287138525",
    "PT87847144161279936872891",
    "QA58DOHB00001234567890ABCDEFG",
    "RO17KRBO3212835705756123",
    "RS80246113647338221492",
    "SA0979413508515371577583",
    "SC18SSCB11010000000000001497USD",
    "SE0651297191201320278580",
    "SI75412441865827872",
    "SK7336568401664473733427",
    "SM67H0718392993037614346095",
    "ST32000200010192194210112",
    "SV62CENR00000000000000700025",
    "TL380080012345678910157",
    "TN6199977105796904072050",
    "TR330006100519786457841326",
    "TR888859050625760496700846",
    "UA213223130000026007233566001",
    "VA59001123000012345678",
    "BE71096123456769",
    "FR7630006000011234567890189",
    "DE91100000000123456789",
    "GR9608100010000001234567890",
    "RO09BCYP0000001234567890",
    "SA4420000001234567891234",
    "ES7921000813610123456789",
    "CH5604835012345678009",
    "GB98MIDL07009312345678",
    "VG14NDUM4605555206975725",
    "XK051212012345678906",
    "BE71 0961 2345 6769",
    "FR76 3000 6000 0112 3456 7890 189",
    "DE91 1000 0000 0123 4567 89",
    "GR96 0810 0010 0000 0123 4567 890",
    "RO09 BCYP 0000 0012 3456 7890",
    "SA44 2000 0001 2345 6789 1234",
    "ES79 2100 0813 6101 2345 6789",
    "CH56 0483 5012 3456 7800 9",
    "GB98 MIDL 0700 9312 3456 78"
  )

  implicit val arbMoney: Arbitrary[Money] = Arbitrary(
    Gen.choose(0L, 1000000000000L).map(b => Money(BigDecimal(b).setScale(2)))
  )

  it should "validate IBAN numbers from a series of concrete examples" in {
    forAll(Gen.oneOf(ibanList), minSuccessful(PosInt(86))) { source: String =>
      IBAN.of(source) shouldBe defined
    }
  }

  it should "store percentages as floats and initialise percent monoids with float monoids" in {
    Monoid[Percent].empty shouldEqual Monoid[Float].empty
  }

  it should "add up percentages using monoidal syntax" in {

    val generator = for {
      p1 <- Gen.chooseNum[Float](0f, 100f)
      p2  = 100f - p1
    } yield p1 -> p2

    forAll(generator) { case (p1, p2) =>
      whenever(p1 >= 0f && p2 >= 0f && BigDecimal(p1.toString).scale <= 3 && BigDecimal(p2.toString).scale <= 3) {
        val addedPercent = Monoid.combineAll(Seq(Percent(p1), Percent(p2)))
        val addedBytes   = Monoid.combineAll(Seq(p1, p2))
        addedPercent shouldEqual Percent(addedBytes)
      }
    }
  }

  it should "fail to construct a type with a scale of more than 3" in {
    an[IllegalArgumentException] should be thrownBy Percent.apply(1.1234f)
  }

  it should "combine and empty Money correctly" in {
    import Money.mon
    forAll { (a: Money, b: Money) =>
      a.combine(b) - a     shouldEqual b
      a.combine(b) - b     shouldEqual a
      a.combine(b) - b - a shouldEqual mon.empty
    }
  }

}
