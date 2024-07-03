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
import enumeratum.EnumFormats
import models.registration._
import models.returns.Return
import play.api.libs.json._
import shapeless.tag.@@

import java.time.LocalDate
import java.time.format.DateTimeParseException
import scala.collection.immutable.ListMap
import play.api.libs.json.Json.fromJson

trait SimpleJson {

  def validatedStringFormat(A: ValidatedType[String], name: String): Format[String @@ A.Tag] =
    new Format[String @@ A.Tag] {
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

  implicit val postcodeFormat: Format[String @@ models.Postcode.Tag]                                   = validatedStringFormat(Postcode, "postcode")
  implicit val phoneNumberFormat: Format[String @@ models.PhoneNumber.Tag]                             =
    validatedStringFormat(PhoneNumber, "phone number")
  implicit val utrFormat: Format[String @@ models.UTR.Tag]                                             = validatedStringFormat(UTR, "UTR")
  implicit val safeIfFormat: Format[String @@ models.SafeId.Tag]                                       = validatedStringFormat(SafeId, "SafeId")
  implicit val formBundleNoFormat: Format[String @@ models.FormBundleNumber.Tag]                       =
    validatedStringFormat(FormBundleNumber, "FormBundleNumber")
  implicit val internalIdFormat: Format[String @@ models.InternalId.Tag]                               =
    validatedStringFormat(InternalId, "internal id")
  implicit val emailFormat: Format[String @@ models.Email.Tag]                                         = validatedStringFormat(Email, "email")
  implicit val countryCodeFormat: Format[String @@ models.CountryCode.Tag]                             =
    validatedStringFormat(CountryCode, "country code")
  implicit val sortCodeFormat: Format[String @@ models.SortCode.Tag]                                   = validatedStringFormat(SortCode, "sort code")
  implicit val accountNumberFormat: Format[String @@ models.AccountNumber.Tag]                         =
    validatedStringFormat(AccountNumber, "account number")
  implicit val buildingSocietyRollNumberFormat: Format[String @@ models.BuildingSocietyRollNumber.Tag] =
    validatedStringFormat(BuildingSocietyRollNumber, "building society roll number")
  implicit val accountNameFormat: Format[String @@ models.AccountName.Tag]                             =
    validatedStringFormat(AccountName, "account name")
  implicit val periodKeyFormat: Format[String @@ Period.Key.Tag]                                       = validatedStringFormat(Period.Key, "Period Key")
  implicit val restrictiveFormat: Format[String @@ models.RestrictiveString.Tag]                       =
    validatedStringFormat(RestrictiveString, "name")
  implicit val companyNameFormat: Format[String @@ models.CompanyName.Tag]                             =
    validatedStringFormat(CompanyName, "company name")
  implicit val mandatoryAddressLineFormat: Format[String @@ models.AddressLine.Tag]                    =
    validatedStringFormat(AddressLine, "address line")
  implicit val dstRegNoFormat: Format[String @@ models.DSTRegNumber.Tag]                               =
    validatedStringFormat(DSTRegNumber, "Digital Services Tax Registration Number")
  implicit val ibanFormat: Format[String @@ models.IBAN.Tag]                                           = validatedStringFormat(IBAN, "IBAN number")

  implicit val moneyFormat: Format[Money] = new Format[Money] {
    override def reads(json: JsValue): JsResult[Money] =
      json match {
        case JsNumber(value) =>
          Money.validateAndTransform(value.setScale(2)) match {
            case Some(validCode) => JsSuccess(Money(validCode))
            case None            => JsError(s"Expected a valid monetary value, got $value instead.")
          }

        case xs: JsValue =>
          JsError(
            JsPath -> JsonValidationError(Seq(s"""Expected a valid monetary value, got $xs instead"""))
          )
      }

    override def writes(o: Money): JsValue = JsNumber(o)
  }

  implicit val percentFormat: Format[Percent] = new Format[Percent] {
    override def reads(json: JsValue): JsResult[Percent] =
      json match {
        case JsNumber(value) =>
          Percent.validateAndTransform(value.toFloat) match {
            case Some(validCode) => JsSuccess(Percent(validCode))
            case None            => JsError(s"Expected a valid percentage, got $value instead.")
          }

        case xs: JsValue =>
          JsError(
            JsPath -> JsonValidationError(Seq(s"""Expected a valid percentage, got $xs instead"""))
          )
      }

    override def writes(o: Percent): JsValue = JsNumber(BigDecimal(o.toString))
  }

}

object SimpleJson extends SimpleJson {
  implicit val companyFormat: OFormat[Company]                     = Json.format[Company]
  implicit val contactDetailsFormat: OFormat[ContactDetails]       = Json.format[ContactDetails]
  implicit val companyRegWrapperFormat: OFormat[CompanyRegWrapper] = Json.format[CompanyRegWrapper]
  implicit val registrationFormat: OFormat[Registration]           = Json.format[Registration]
  implicit val activityFormat: Format[Activity]                    = EnumFormats.formats(Activity)
  implicit val groupCompanyFormat: Format[GroupCompany]            = Json.format[GroupCompany]

  implicit val activityMapFormat: Format[Map[Activity, Percent]] = new Format[Map[Activity, Percent]] {
    override def reads(json: JsValue): JsResult[Map[Activity, Percent]] =
      JsSuccess(json.as[Map[String, JsNumber]].map { case (k, v) =>
        Activity.values.find(_.entryName == k).get -> Percent.apply(v.value.toFloat)
      })

    override def writes(o: Map[Activity, Percent]): JsValue =
      JsObject(o.toSeq.map { case (k, v) =>
        k.entryName -> JsNumber(BigDecimal(v.toString))
      })
  }

  implicit def listMapReads[V](implicit formatV: Reads[V]): Reads[ListMap[String, V]] = new Reads[ListMap[String, V]] {
    def reads(json: JsValue) = json match {
      case JsObject(m) =>
        type Errors = scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]

        def locate(e: Errors, key: String): scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] =
          e.map { case (path, validationError) =>
            (JsPath \ key) ++ path -> validationError
          }

        m.foldLeft(Right(ListMap.empty): Either[Errors, ListMap[String, V]]) { case (acc, (key, value)) =>
          (acc, fromJson[V](value)(formatV)) match {
            case (Right(vs), JsSuccess(v, _)) => Right(vs + (key -> v))
            case (Right(_), JsError(e))       => Left(locate(e, key))
            case (Left(e), _: JsSuccess[_])   => Left(e)
            case (Left(e1), JsError(e2))      => Left(e1 ++ locate(e2, key))
          }
        }.fold(_ => JsError.apply(), res => JsSuccess(res))

      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("error.expected.jsobject"))))
    }
  }

  implicit val groupCompanyMapFormat: OFormat[ListMap[GroupCompany, Money]] =
    new OFormat[ListMap[GroupCompany, Money]] {
      override def reads(json: JsValue): JsResult[ListMap[GroupCompany, Money]] =
        JsSuccess(json.as[ListMap[String, JsNumber]].map { case (k, v) =>
          k.split(":") match {
            case Array(name, utrS) =>
              GroupCompany(CompanyName(name), Some(UTR(utrS))) -> Money.apply(v.value.setScale(2))
            case Array(name)       =>
              GroupCompany(CompanyName(name), None) -> Money.apply(v.value.setScale(2))
          }
        })

      override def writes(o: ListMap[GroupCompany, Money]): JsObject =
        JsObject(o.toSeq.map { case (k, v) =>
          s"${k.name}:${k.utr.getOrElse("")}" -> JsNumber(v)
        })
    }

  implicit val repaymentDetailsFormat: OFormat[RepaymentDetails] = Json.format[RepaymentDetails]
  implicit val returnFormat: OFormat[Return]                     = Json.format[Return]

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
    (o: List[(Period, Option[LocalDate])]) => {

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
        Seq {
          "obligations" -> JsArray(details.map { dt =>
            JsObject(
              Seq(
                "obligationDetails" -> dt
              )
            )
          })
        }
      )
    }

  implicit lazy val readPeriods: Reads[List[(Period, Option[LocalDate])]] =
    (jsonOuter: JsValue) => {
      val JsArray(obligations) = {
        jsonOuter \ "obligations"
      }.as[JsArray]
      val periods              = obligations.toList.flatMap { j =>
        val JsArray(elems) = {
          j \ "obligationDetails"
        }.as[JsArray]
        elems.toList
      }
      JsSuccess(periods.map { json =>
        (
          Period(
            {
              json \ "inboundCorrespondenceFromDate"
            }.as[LocalDate], {
              json \ "inboundCorrespondenceToDate"
            }.as[LocalDate], {
              json \ "inboundCorrespondenceDueDate"
            }.as[LocalDate], {
              json \ "periodKey"
            }.as[Period.Key]
          ), {
            json \ "inboundCorrespondenceDateReceived"
          }.asOpt[LocalDate]
        )
      })
    }

}
