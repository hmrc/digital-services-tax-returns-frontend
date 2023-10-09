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

package utils

import akka.actor.ActorSystem
import config.FrontendAppConfig
import connectors.DSTConnector
import controllers.actions.AuthorisedAction
import org.scalatest.TryValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.{BaseOneAppPerSuite, FakeApplicationFactory, PlaySpec}
import play.api.i18n.{Lang, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.MessagesControllerComponents
import play.api.{Application, Configuration, Environment}
import test.TestConnector
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import views.html.ReturnsDashboardView
import views.html.layouts.GovukLayoutWrapper

import java.io.File
import java.time.Clock
import scala.concurrent.duration._

trait FakeApplicationSetup
    extends PlaySpec
    with BaseOneAppPerSuite
    with FakeApplicationFactory
    with TryValues
    with ScalaFutures {

  implicit lazy val actorSystem: ActorSystem   = app.actorSystem
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = 5.seconds, interval = 100.millis)
  implicit val clock: Clock                    = Clock.systemDefaultZone()
  implicit val lang: Lang                      = Lang("en")

  lazy val environment: Environment     = Environment.simple(new File("."))
  lazy val configuration: Configuration = Configuration.load(environment)

  lazy val messagesApi: MessagesApi                         = app.injector.instanceOf[MessagesApi]
  lazy val wsClient: WSClient                               = app.injector.instanceOf[WSClient]
  lazy val httpAuditing: HttpAuditing                       = app.injector.instanceOf[HttpAuditing]
  lazy val httpClient: HttpClient                           = new DefaultHttpClient(configuration, httpAuditing, wsClient, actorSystem)
  lazy val authorisedAction: AuthorisedAction               = app.injector.instanceOf[AuthorisedAction]
  lazy val authConnector: PlayAuthConnector                 = new DefaultAuthConnector(httpClient, servicesConfig)
  lazy val layoutInstance: GovukLayoutWrapper                           = app.injector.instanceOf[GovukLayoutWrapper]
  lazy val landingInstance: ReturnsDashboardView                         = app.injector.instanceOf[ReturnsDashboardView]

  lazy val appConfig: FrontendAppConfig           = app.injector.instanceOf[FrontendAppConfig]
  val servicesConfig: ServicesConfig      = app.injector.instanceOf[ServicesConfig]
  lazy val mockDSTConnector: DSTConnector = mock[DSTConnector]

  val testConnector: TestConnector = new TestConnector(httpClient, servicesConfig)

  override def fakeApplication(): Application =
    GuiceApplicationBuilder(environment = environment)
      .configure(Map("tax-enrolments.enabled" -> "true"))
      .build()

  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val fakeAuthorisedAction = new FakeAuthorisedAction(mcc, authConnector)(appConfig, implicitly, messagesApi)
}
