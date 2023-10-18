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
import models.registration._
import play.api.libs.json._
import shapeless.tag.@@

import java.time.LocalDate
import java.time.format.DateTimeParseException

trait SimpleJson {

  def validatedStringFormat(A: ValidatedType[String], name: String) = new Format[String @@ A.Tag] {
    override def reads(json: JsValue): JsResult[String @@ A.Tag] = json match {
      case JsString(value) =>
        A.validateAndTransform(value) match {
          case Some(v) => JsSuccess(A(v))
          case None    => JsError(s"Expected a valid $name, got $value instead")
        }
      case xs: JsValue     => JsError(JsPath -> JsonValidationError(Seq(s"""Expected a valid $name, got $xs instead""")))
    }

    override def writes(o: String @@ A.Tag): JsValue = JsString(o)
  }

  implicit val postcodeFormat                  = validatedStringFormat(Postcode, "postcode")
  implicit val phoneNumberFormat               = validatedStringFormat(PhoneNumber, "phone number")
  implicit val utrFormat                       = validatedStringFormat(UTR, "UTR")
  implicit val safeIfFormat                    = validatedStringFormat(SafeId, "SafeId")
  implicit val formBundleNoFormat              = validatedStringFormat(FormBundleNumber, "FormBundleNumber")
  implicit val internalIdFormat                = validatedStringFormat(InternalId, "internal id")
  implicit val emailFormat                     = validatedStringFormat(Email, "email")
  implicit val countryCodeFormat               = validatedStringFormat(CountryCode, "country code")
  implicit val sortCodeFormat                  = validatedStringFormat(SortCode, "sort code")
  implicit val accountNumberFormat             = validatedStringFormat(AccountNumber, "account number")
  implicit val buildingSocietyRollNumberFormat =
    validatedStringFormat(BuildingSocietyRollNumber, "building society roll number")
  implicit val accountNameFormat               = validatedStringFormat(AccountName, "account name")
  implicit val periodKeyFormat                 = validatedStringFormat(Period.Key, "Period Key")
  implicit val restrictiveFormat               = validatedStringFormat(RestrictiveString, "name")
  implicit val companyNameFormat               = validatedStringFormat(CompanyName, "company name")
  implicit val mandatoryAddressLineFormat      = validatedStringFormat(AddressLine, "address line")
  implicit val dstRegNoFormat                  = validatedStringFormat(DSTRegNumber, "Digital Services Tax Registration Number")

}

object BackendAndFrontendJson extends SimpleJson {
  implicit val companyFormat: OFormat[Company]                     = Json.format[Company]
  implicit val contactDetailsFormat: OFormat[ContactDetails]       = Json.format[ContactDetails]
  implicit val companyRegWrapperFormat: OFormat[CompanyRegWrapper] = Json.format[CompanyRegWrapper]
  implicit val registrationFormat: OFormat[Registration]           = Json.format[Registration]

  implicit val periodFormat: OFormat[Period] = Json.format[Period]

  implicit lazy val basicDateFormatWrites: Format[LocalDate] = new Format[LocalDate] {

    def writes(dt: LocalDate): JsValue = JsString(dt.toString)

    def reads(i: JsValue): JsResult[LocalDate] = i match {
      case JsString(s) =>
        Either
          .catchOnly[DateTimeParseException] {
            LocalDate.parse(s)
          }
          .fold[JsResult[LocalDate]](e => JsError(e.getLocalizedMessage), JsSuccess(_))
      case o           => JsError(s"expected a JsString(YYYY-MM-DD), got a $o")
    }
  }

  implicit lazy val writePeriods: Writes[List[(Period, Option[LocalDate])]] =
    new Writes[List[(Period, Option[LocalDate])]] {
      override def writes(o: List[(Period, Option[LocalDate])]): JsValue = {

        val details = o.map { case (period, mapping) =>
          JsObject(
            Seq(
              "inboundCorrespondenceFromDate"     -> Json.toJson(period.start),
              "inboundCorrespondenceToDate"       -> Json.toJson(period.end),
              "inboundCorrespondenceDueDate"      -> Json.toJson(period.returnDue),
              "periodKey"                         -> Json.toJson(period.key),
              "inboundCorrespondenceDateReceived" -> Json.toJson(mapping)
            )
          )

        }

        JsObject(
          Seq(
            "obligations" -> JsArray(details.map { dt =>
              JsObject(
                Seq(
                  "obligationDetails" -> dt
                )
              )
            })
          )
        )
      }
    }

  implicit lazy val readPeriods: Reads[List[(Period, Option[LocalDate])]] =
    new Reads[List[(Period, Option[LocalDate])]] {
      def reads(jsonOuter: JsValue): JsResult[List[(Period, Option[LocalDate])]] = {
        val JsArray(obligations) = { jsonOuter \ "obligations" }.as[JsArray]
        val periods              = obligations.toList.flatMap { j =>
          val JsArray(elems) = { j \ "obligationDetails" }.as[JsArray]
          elems.toList
        }
        JsSuccess(periods.map { json =>
          (
            Period(
              { json \ "inboundCorrespondenceFromDate" }.as[LocalDate],
              { json \ "inboundCorrespondenceToDate" }.as[LocalDate],
              { json \ "inboundCorrespondenceDueDate" }.as[LocalDate],
              { json \ "periodKey" }.as[Period.Key]
            ),
            { json \ "inboundCorrespondenceDateReceived" }.asOpt[LocalDate]
          )
        })
      }
    }

}
