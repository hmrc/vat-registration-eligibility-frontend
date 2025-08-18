/*
 * Copyright 2024 HM Revenue & Customs
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

import com.typesafe.config.{ConfigList, ConfigRenderOptions}
import featureswitch.core.config.FeatureSwitching
import play.api.Configuration
import models.VatThreshold
import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject()(val servicesConfig: ServicesConfig, configuration: Configuration) extends FeatureSwitching {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  lazy val timeout: Int = servicesConfig.getInt("timeout.timeout")
  lazy val countdown: Int = servicesConfig.getInt("timeout.countdown")

  private lazy val contactFrontendUrl: String = loadConfig("microservice.services.contact-frontend.url")
  lazy val contactFormServiceIdentifier = "vrs"

  lazy val appName = loadConfig(s"appName")
  lazy val betaFeedbackUrl = s"$contactFrontendUrl/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val loginUrl = loadConfig("urls.login")
  private val configRoot = "microservice.services"
  lazy val vatRegFEURL = loadConfig(s"$configRoot.vat-registration-frontend.url")
  lazy val vatRegFEURI = loadConfig(s"$configRoot.vat-registration-frontend.uri")
  lazy val postSignInUrl = loadConfig(s"$configRoot.vat-registration-frontend.postSignInUrl")
  lazy val feedbackFrontendUrl = loadConfig(s"$configRoot.feedback-frontend.url")
  lazy val exitSurveyUrl = s"$feedbackFrontendUrl/feedback/vat-registration"
  lazy val VAT1AFormURL = servicesConfig.getConfString("gov-uk.VAT1AFormURL", throw new Exception("Couldn't get VAT1AFormURL URL"))
  lazy val VAT1CFormURL = servicesConfig.getConfString("gov-uk.VAT1CFormURL", throw new Exception("Couldn't get VAT1CFormURL URL"))
  lazy val VAT98FormURL = servicesConfig.getConfString("gov-uk.VAT98FormURL", throw new Exception("Couldn't get VAT98FormURL URL"))
  lazy val VATNotice700_46agriculturalURL = servicesConfig.getConfString("gov-uk.VATNotice700_46agriculturalURL",
    throw new Exception("Couldn't get VATNotice700_46agriculturalURL URL"))
  lazy val VATMtdInformationGroup = servicesConfig.getConfString("gov-uk.VATMtdInformationGroup",
    throw new Exception("Couldn't get VATMtdInformationGroup URL"))
  lazy val VATDivisionURL = servicesConfig.getConfString("gov-uk.VATDivisionURL", throw new Exception("Couldn't get VATDivisionURL URL"))
  lazy val thresholdString: String = configuration.get[ConfigList]("vat-threshold").render(ConfigRenderOptions.concise())
  lazy val thresholds: Seq[VatThreshold] = Json.parse(thresholdString).as[List[VatThreshold]]
  lazy val calculateTurnoverUrl = "https://www.gov.uk/vat-registration/calculate-turnover"

  lazy val accessibilityStatementUrl = servicesConfig.getString("accessibility-statement.host") + "/accessibility-statement/vat-registration"
}
