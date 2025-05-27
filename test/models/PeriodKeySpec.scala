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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class PeriodKeySpec extends AnyFreeSpec with Matchers with OptionValues {

  "PeriodKeyBindable" - {
    val binder = PeriodKey.pathBindable
    val key    = "003"

    "bind a valid index" in {
      binder.bind(key, "001") mustBe Right(PeriodKey("001"))
    }

    "fail to bind an index with negative value" in {
      binder.bind(key, "gahsdgajsgdjha") mustBe Left("PeriodKey binding failed")
    }

    "unbind an index" in {
      binder.unbind(key, PeriodKey("001")) mustEqual "001"
    }
  }
}
