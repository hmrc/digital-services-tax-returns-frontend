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

package config

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ServiceSpec extends AnyFreeSpec with Matchers {

  "Service" - {
    "must build the base Url from he input" in {
      val service = Service("host", "port", "protocol")
      service.baseUrl mustBe "protocol://host:port"
      service.toString mustBe "protocol://host:port"
      Service.convertToString(service) mustBe "protocol://host:port"
    }
  }
}
