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

package models.registration

import models.{AddressLine, CountryCode, Postcode, SimpleJson}
import play.api.libs.json.{Format, JsResult, JsValue, Json, OFormat}

sealed trait Address {
  def line1: AddressLine
  def line2: Option[AddressLine]
  def line3: Option[AddressLine]
  def line4: Option[AddressLine]
  def countryCode: CountryCode
  def postalCode: String
}

final case class UkAddress(
  line1: AddressLine,
  line2: Option[AddressLine],
  line3: Option[AddressLine],
  line4: Option[AddressLine],
  postalCode: Postcode
) extends Address {
  def countryCode = CountryCode("GB")
}

final case class ForeignAddress(
  line1: AddressLine,
  line2: Option[AddressLine],
  line3: Option[AddressLine],
  line4: Option[AddressLine],
  countryCode: CountryCode
) extends Address {
  def postalCode: String = ""
}

object Address extends SimpleJson {

  implicit val foreignAddressFormat: OFormat[ForeignAddress] = Json.format[ForeignAddress]
  implicit val ukAddressFormat: OFormat[UkAddress] = Json.format[UkAddress]

  implicit val format: Format[Address] = new Format[Address] {

    override def reads(json: JsValue): JsResult[Address] =
      json
        .validate[UkAddress]
        .orElse(
          json.validate[ForeignAddress]
        )

    override def writes(o: Address): JsValue = o match {
      case m:UkAddress => ukAddressFormat.writes(m)
      case m:ForeignAddress => foreignAddressFormat.writes(m)
    }
  }
}
