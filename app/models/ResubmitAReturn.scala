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

package models

import models.registration.Period
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

case class ResubmitAReturn(key: String)
object ResubmitAReturn {

  implicit val format: OFormat[ResubmitAReturn] = Json.format[ResubmitAReturn]

  def options(periods: Seq[Period])(implicit messages: Messages): Seq[RadioItem] = periods.zipWithIndex.map {
    case (period, index) =>
      RadioItem(
        content = Text(messages("resubmitAReturn.radio-label", formatDate(period.start), formatDate(period.end))),
        id = Some(s"value_$index"),
        value = Some(s"${period.key}")
      )
  }
}
