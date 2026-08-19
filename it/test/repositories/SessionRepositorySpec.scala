/*
 * Copyright 2026 HM Revenue & Customs
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

package repositories

import config.{FakeEncrypterDecrypter, FrontendAppConfig}
import models.{SensitiveWrapper, UserAnswers, UserAnswersEncrypted}
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import repositories.SessionRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswersEncrypted]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))
  private val userAnswersEncrypted =
    UserAnswersEncrypted(userAnswers.id, SensitiveWrapper(userAnswers.data), userAnswers.lastUpdated)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl).thenReturn(1L)

  private val fakeCrypto: Encrypter & Decrypter = new FakeEncrypterDecrypter()

  protected override val repository: SessionRepository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock,
    crypto = fakeCrypto
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult =
        UserAnswersEncrypted(userAnswers.id, SensitiveWrapper(userAnswers.data), instant)

      repository.set(userAnswers).futureValue mustEqual true
      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value
      
      updatedRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(userAnswersEncrypted).futureValue

        val result         = repository.get(userAnswers.id).futureValue
        val expectedResult = userAnswers.copy(lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {

      insert(userAnswersEncrypted).futureValue

      repository.clear(userAnswers.id).futureValue mustEqual true

      repository.get(userAnswers.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(userAnswersEncrypted).futureValue

        repository.keepAlive(userAnswers.id).futureValue mustEqual true

        val expectedUpdatedAnswers =
          UserAnswersEncrypted(userAnswers.id, SensitiveWrapper(userAnswers.data), instant)
        
        val updatedAnswers = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }
  }
}
