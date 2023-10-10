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

package config

import com.google.inject.{Inject, Singleton}
import models.registration.Period
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  private def loadConfig(key: String) = configuration.get[String](key)

  val dstFrontendBaseUrl: String = servicesConfig.baseUrl("digital-services-tax-frontend")
  val dstFrontendRegistrationUrl: String = dstFrontendBaseUrl + "/register"
  val dstFrontendShowAmendmentsPageUrl: String = dstFrontendBaseUrl + "/resubmit-a-return"
  val dstFrontendReturnActionUrl: String = dstFrontendBaseUrl + s"/submit-return/###"

  lazy val dstIndexPage: String        = loadConfig("dst-index-page-url")
  lazy val ggLoginUrl: String                     = s"$companyAuthFrontend$companyAuthSignInPath"
  lazy val feedbackSurveyUrl: String   = loadConfig("microservice.services.feedback-survey.url")
  private lazy val companyAuthFrontend: String    = servicesConfig.getConfString("company-auth.url", "")
  private lazy val companyAuthSignInPath: String  = servicesConfig.getConfString("company-auth.sign-in-path", "")
  private lazy val companyAuthSignOutPath: String = servicesConfig.getConfString("company-auth.sign-out-path", "")

  lazy val signOutDstUrl: String       = s"$companyAuthFrontend$companyAuthSignOutPath?continue=$feedbackSurveyUrl"

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

<<<<<<< HEAD
  val contactHost = configuration.get[String]("contact-frontend.host")
=======
  private val contactHost                  = configuration.get[String]("contact-frontend.host")
>>>>>>> 68353df32c95a28256af2aef153092067a8189fe
  private val contactFormServiceIdentifier = "digital-services-tax-returns-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/digital-services-tax-returns-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")
}
