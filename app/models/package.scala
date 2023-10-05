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

import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.{TimeZone, ULocale}
import fr.marcwrobel.jbanking.iban.Iban
import play.api.libs.json._
import shapeless.tag.@@

import java.time.{LocalDate, ZoneId}

package object models {
  private val zone             = "Europe/London"
  val zoneId: ZoneId           = ZoneId.of(zone)

  def formatDate(localDate: LocalDate, dateFormatPattern: String = "d MMMM yyyy"): String = {
    val date = java.util.Date.from(localDate.atStartOfDay(zoneId).toInstant)
    createDateFormatForPattern(dateFormatPattern).format(date)
  }

  def createDateFormatForPattern(pattern: String): SimpleDateFormat = {
    val locale: ULocale = ULocale.getDefault
    val sdf             = new SimpleDateFormat(pattern, locale)
    sdf.setTimeZone(TimeZone.getTimeZone(zone))
    sdf
  }

  type UTR = String @@ UTR.Tag
  object UTR
    extends RegexValidatedString(
      "^[0-9]{10}$",
      _.replaceAll(" ", "")
    )

  type Percent = Float @@ Percent.Tag
  object Percent extends ValidatedType[Float] {
    def validateAndTransform(in: Float): Option[Float] =
      Some(in).filter { x =>
        (x >= 0 && x <= 100) && (BigDecimal(x.toString).scale <= 3)
      }
  }

  type Email = String @@ Email.Tag
  object Email extends ValidatedType[String] {
    def validateAndTransform(email: String): Option[String] = {
      import org.apache.commons.validator.routines.EmailValidator
      Some(email).filter(EmailValidator.getInstance.isValid(_))
    }
  }

  type InternalId = String @@ InternalId.Tag
  object InternalId
    extends RegexValidatedString(
      regex = "^Int-[a-f0-9-]*$"
    )

  type Postcode = String @@ Postcode.Tag
  object Postcode
    extends RegexValidatedString(
      """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$""",
      _.trim.replaceAll("[ \\t]+", " ").toUpperCase
    )

  type Money = BigDecimal @@ Money.Tag
  object Money extends ValidatedType[BigDecimal] {
    def validateAndTransform(in: BigDecimal): Option[BigDecimal] =
      Some(in).filter(_.toString.matches("^[0-9]+(\\.[0-9]{1,2})?$"))
  }

  type PhoneNumber = String @@ PhoneNumber.Tag
  object PhoneNumber
    extends RegexValidatedString(
      "^[A-Z0-9 \\-]{1,30}$"
    )

  type CompanyName = String @@ CompanyName.Tag
  object CompanyName
    extends RegexValidatedString(
      regex = """^[a-zA-Z0-9 '&.-]{1,105}$"""
    )

  type AddressLine = String @@ AddressLine.Tag
  object AddressLine
    extends RegexValidatedString(
      regex = """^[a-zA-Z0-9 '&.-]{1,35}$"""
    )

  type IBAN = String @@ IBAN.Tag
  object IBAN extends ValidatedType[String] {
    override def validateAndTransform(in: String): Option[String] =
      Some(in).map(_.replaceAll("\\s+", "")).filter(Iban.isValid)
  }

  type SafeId = String @@ SafeId.Tag
  object SafeId
    extends RegexValidatedString(
      "^[A-Z0-9]{1,15}$"
    )

  type SortCode = String @@ SortCode.Tag
  object SortCode
    extends RegexValidatedString(
      """^[0-9]{6}$""",
      _.filter(_.isDigit)
    )

  type AccountNumber = String @@ AccountNumber.Tag
  object AccountNumber
    extends RegexValidatedString(
      """^[0-9]{8}$""",
      _.filter(_.isDigit)
    )

  type BuildingSocietyRollNumber = String @@ BuildingSocietyRollNumber.Tag
  object BuildingSocietyRollNumber
    extends RegexValidatedString(
      """^[A-Za-z0-9 -]{1,18}$"""
    )

  type AccountName = String @@ AccountName.Tag
  object AccountName
    extends RegexValidatedString(
      """^[a-zA-Z&^]{1,35}$"""
    )

  type DSTRegNumber = String @@ DSTRegNumber.Tag
  object DSTRegNumber
    extends RegexValidatedString(
      "^([A-Z]{2}DST[0-9]{10})$"
    )

  type RestrictiveString = String @@ RestrictiveString.Tag
  object RestrictiveString
    extends RegexValidatedString(
      """^[a-zA-Z'&-^]{1,35}$"""
    )

  type CountryCode = String @@ CountryCode.Tag
  object CountryCode
    extends RegexValidatedString(
      """^[A-Z][A-Z]$""",
      _.toUpperCase match {
        case "UK"  => "GB"
        case other => other
      }
    )

  type NonEmptyString = String @@ NonEmptyString.Tag
  object NonEmptyString extends ValidatedType[String] {
    def validateAndTransform(in: String): Option[String] =
      Some(in).filter(_.nonEmpty)
  }

  type FormBundleNumber = String @@ FormBundleNumber.Tag
  object FormBundleNumber
    extends RegexValidatedString(
      regex = "^[0-9]{12}$"
    )


  implicit class RichJsObject(jsObject: JsObject) {

    def setObject(path: JsPath, value: JsValue): JsResult[JsObject] =
      jsObject.set(path, value).flatMap(_.validate[JsObject])

    def removeObject(path: JsPath): JsResult[JsObject] =
      jsObject.remove(path).flatMap(_.validate[JsObject])
  }

  implicit class RichJsValue(jsValue: JsValue) {

    def set(path: JsPath, value: JsValue): JsResult[JsValue] =
      (path.path, jsValue) match {

        case (Nil, _) =>
          JsError("path cannot be empty")

        case ((_: RecursiveSearch) :: _, _) =>
          JsError("recursive search not supported")

        case ((n: IdxPathNode) :: Nil, _) =>
          setIndexNode(n, jsValue, value)

        case ((n: KeyPathNode) :: Nil, _) =>
          setKeyNode(n, jsValue, value)

        case (first :: second :: rest, oldValue) =>
          Reads.optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue).flatMap {
            opt =>

              opt.map(JsSuccess(_)).getOrElse {
                second match {
                  case _: KeyPathNode =>
                    JsSuccess(Json.obj())
                  case _: IdxPathNode =>
                    JsSuccess(Json.arr())
                  case _: RecursiveSearch =>
                    JsError("recursive search is not supported")
                }
              }.flatMap {
                _.set(JsPath(second :: rest), value).flatMap {
                  newValue =>
                    oldValue.set(JsPath(first :: Nil), newValue)
                }
              }
          }
      }

    private def setIndexNode(node: IdxPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val index: Int = node.idx

      oldValue match {
        case oldValue: JsArray if index >= 0 && index <= oldValue.value.length =>
          if (index == oldValue.value.length) {
            JsSuccess(oldValue.append(newValue))
          } else {
            JsSuccess(JsArray(oldValue.value.updated(index, newValue)))
          }
        case oldValue: JsArray =>
          JsError(s"array index out of bounds: $index, $oldValue")
        case _ =>
          JsError(s"cannot set an index on $oldValue")
      }
    }

    private def removeIndexNode(node: IdxPathNode, valueToRemoveFrom: JsArray): JsResult[JsValue] = {
      val index: Int = node.idx

      valueToRemoveFrom match {
        case valueToRemoveFrom: JsArray if index >= 0 && index < valueToRemoveFrom.value.length =>
          val updatedJsArray = valueToRemoveFrom.value.slice(0, index) ++ valueToRemoveFrom.value.slice(index + 1, valueToRemoveFrom.value.size)
          JsSuccess(JsArray(updatedJsArray))
        case valueToRemoveFrom: JsArray => JsError(s"array index out of bounds: $index, $valueToRemoveFrom")
        case _ => JsError(s"cannot set an index on $valueToRemoveFrom")
      }
    }

    private def setKeyNode(node: KeyPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val key = node.key

      oldValue match {
        case oldValue: JsObject =>
          JsSuccess(oldValue + (key -> newValue))
        case _ =>
          JsError(s"cannot set a key on $oldValue")
      }
    }

    def remove(path: JsPath): JsResult[JsValue] = {

      (path.path, jsValue) match {
        case (Nil, _) => JsError("path cannot be empty")
        case ((n: KeyPathNode) :: Nil, value: JsObject) if value.keys.contains(n.key) => JsSuccess(value - n.key)
        case ((n: KeyPathNode) :: Nil, value: JsObject) if !value.keys.contains(n.key) => JsError("cannot find value at path")
        case ((n: IdxPathNode) :: Nil, value: JsArray) => removeIndexNode(n, value)
        case ((_: KeyPathNode) :: Nil, _) => JsError(s"cannot remove a key on $jsValue")
        case (first :: second :: rest, oldValue) =>

          Reads.optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue).flatMap {
            opt: Option[JsValue] =>

              opt.map(JsSuccess(_)).getOrElse {
                second match {
                  case _: KeyPathNode =>
                    JsSuccess(Json.obj())
                  case _: IdxPathNode =>
                    JsSuccess(Json.arr())
                  case _: RecursiveSearch =>
                    JsError("recursive search is not supported")
                }
              }.flatMap {
                _.remove(JsPath(second :: rest)).flatMap {
                  newValue =>
                    oldValue.set(JsPath(first :: Nil), newValue)
                }
              }
          }
      }
    }
  }
}
