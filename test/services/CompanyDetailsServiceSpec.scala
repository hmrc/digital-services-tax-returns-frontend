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

package services

import base.SpecBase
import models.{CompanyDetails, PeriodKey, UserAnswers}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.libs.json._
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyDetailsServiceSpec extends SpecBase {

  val mockSessionRepository: SessionRepository     = mock[SessionRepository]
  val companyDetailsService: CompanyDetailsService = new CompanyDetailsService(mockSessionRepository)

  "companyDetailsExists" - {
    "must return None when no user answers exist" in {
      when(mockSessionRepository.get("Int-123-456-789")).thenReturn(Future.successful(None))

      whenReady(
        companyDetailsService
          .companyDetailsExists("Int-123-456-789", PeriodKey("001"), CompanyDetails("fun ltd", Some("1234567890")))
      ) { exists =>
        exists mustEqual None
      }
    }

    "must return false when the user answers are not populated" in {
      when(mockSessionRepository.get("Int-123-456-789"))
        .thenReturn(Future.successful(Some(UserAnswers("Int-123-456-789", Json.obj()))))

      whenReady(
        companyDetailsService
          .companyDetailsExists("Int-123-456-789", PeriodKey("001"), CompanyDetails("fun ltd", Some("1234567890")))
      ) { exists =>
        exists.value mustEqual false
      }
    }

    "must return true when company has been added" in {
      val jsObj = Json.obj(
        ("004", Json.obj(("company-details", JsArray(Seq(Json.toJson(CompanyDetails("fun ltd", Some("1234567890"))))))))
      )
      when(mockSessionRepository.get("Int-123-456-789"))
        .thenReturn(Future.successful(Some(UserAnswers("Int-123-456-789", jsObj))))

      whenReady(
        companyDetailsService
          .companyDetailsExists("Int-123-456-789", PeriodKey("004"), CompanyDetails("fun ltd", Some("1234567890")))
      ) { exists =>
        exists.value mustEqual true
      }
    }

    "must return false when the company has not been added" in {
      val jsObj = Json.obj(
        ("004", Json.obj(("company-details", JsArray(Seq(Json.toJson(CompanyDetails("fun ltd", Some("1234567890"))))))))
      )
      when(mockSessionRepository.get("Int-123-456-789"))
        .thenReturn(Future.successful(Some(UserAnswers("Int-123-456-789", jsObj))))

      whenReady(
        companyDetailsService
          .companyDetailsExists("Int-123-456-789", PeriodKey("001"), CompanyDetails("boring ltd", Some("1334557899")))
      ) { exists =>
        exists.value mustEqual false
      }
    }

    "must return false when company details don't match" in {
      val jsObj = Json.obj(
        ("004", Json.obj(("company-details", JsArray(Seq(Json.toJson(CompanyDetails("fun ltd", Some("1234567890"))))))))
      )
      when(mockSessionRepository.get("Int-123-456-789"))
        .thenReturn(Future.successful(Some(UserAnswers("Int-123-456-789", jsObj))))

      whenReady(
        companyDetailsService
          .companyDetailsExists("Int-123-456-789", PeriodKey("001"), CompanyDetails("boring ltd", Some("1134577891")))
      ) { exists =>
        exists.value mustEqual false
      }
    }
  }
}
