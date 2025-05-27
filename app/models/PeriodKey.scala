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

import play.api.mvc.PathBindable

case class PeriodKey(value: String)

object PeriodKey {

  implicit def pathBindable(implicit stringBinder: PathBindable[String]): PathBindable[PeriodKey] =
    new PathBindable[PeriodKey] {

      override def bind(key: String, value: String): Either[String, PeriodKey] =
        stringBinder.bind(key, value) match {
          case Right(x) if x.nonEmpty && x.length <= 4 => Right(PeriodKey(x))
          case _                                       => Left("PeriodKey binding failed")
        }

      override def unbind(key: String, value: PeriodKey): String =
        stringBinder.unbind(key, value.value)
    }
}
