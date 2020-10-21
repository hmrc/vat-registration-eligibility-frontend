/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.routes
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(val runModeConfiguration: Configuration,
                                  val servicesConfig: ServicesConfig
                                 ) {

  lazy val host: String = servicesConfig.getString("host")

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private lazy val contactHost = servicesConfig.baseUrl("contact-frontend")
  private val contactFormServiceIdentifier = "SCRS"

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val loginUrl = loadConfig("urls.login")
  private val configRoot = "microservice.services"
  lazy val vatRegFEURL = loadConfig(s"$configRoot.vat-registration-frontend.url")
  lazy val vatRegFEURI = loadConfig(s"$configRoot.vat-registration-frontend.uri")
  lazy val vatRegFEFirstPage = loadConfig(s"$configRoot.vat-registration-frontend.vrfeFirstPage")
  lazy val postSignInUrl = loadConfig(s"$configRoot.vat-registration-frontend.postSignInUrl")
  lazy val feedbackUrl = loadConfig(s"$configRoot.vat-registration-frontend.feedbackUrl")
  lazy val companyRegUrl = loadConfig(s"$configRoot.company-registration-frontend.url")
  lazy val companyRegUri = loadConfig(s"$configRoot.company-registration-frontend.uri")
  lazy val exitSurveyUri = loadConfig(s"$configRoot.company-registration-frontend.exit-survey")
  lazy val otrsUrl = servicesConfig.getConfString("otrs.url", throw new Exception("Couldn't get otrs URL"))
  lazy val VATNotice700_1supplementURL = servicesConfig.getConfString("gov-uk.VATNotice700_1supplementURL",
    throw new Exception("Couldn't get VATNotice700_1supplementURL URL"))
  lazy val VATAnnualAccountingSchemeURL = servicesConfig.getConfString("gov-uk.VATAnnualAccountingSchemeURL",
    throw new Exception("Couldn't get VATAnnualAccountingSchemeURL URL"))
  lazy val VAT1FormURL = servicesConfig.getConfString("gov-uk.VAT1FormURL", throw new Exception("Couldn't get VAT1FormURL URL"))
  lazy val VAT1BFormURL = servicesConfig.getConfString("gov-uk.VAT1BFormURL", throw new Exception("Couldn't get VAT1BFormURL URL"))
  lazy val VAT1CFormURL = servicesConfig.getConfString("gov-uk.VAT1CFormURL", throw new Exception("Couldn't get VAT1CFormURL URL"))
  lazy val VAT98FormURL = servicesConfig.getConfString("gov-uk.VAT98FormURL", throw new Exception("Couldn't get VAT98FormURL URL"))
  lazy val VATNotice700_46agriculturalURL = servicesConfig.getConfString("gov-uk.VATNotice700_46agriculturalURL",
    throw new Exception("Couldn't get VATNotice700_46agriculturalURL URL"))
  lazy val VATRateDifferentGoodsURL = servicesConfig.getConfString("gov-uk.VATRateDifferentGoodsURL",
    throw new Exception("Couldn't get VATRateDifferentGoodsURL URL"))
  lazy val VATSetUpVATGroup = servicesConfig.getConfString("gov-uk.VATSetUpVATGroup",
    throw new Exception("Couldn't get VATSetUpVATGroup URL"))
  lazy val VATVoluntaryInformationGroup = servicesConfig.getConfString("gov-uk.VATVoluntaryInformationGroup",
    throw new Exception("Couldn't get VATVoluntaryInformationGroup URL"))
  lazy val VATMandatoryInformationGroup = servicesConfig.getConfString("gov-uk.VATMandatoryInformationGroup",
    throw new Exception("Couldn't get VATMandatoryInformationGroup URL"))
  lazy val VATFileChanges = servicesConfig.getConfString("gov-uk.VATFileChanges", throw new Exception("Couldn't get VATFileChanges URL"))
  lazy val languageTranslationEnabled = runModeConfiguration.getBoolean("microservice.services.features.welsh-translation").getOrElse(true)

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  //Footer Links
  lazy val cookies: String = host + servicesConfig.getString("urls.footer.cookies")
  lazy val privacy: String = host + servicesConfig.getString("urls.footer.privacy")
  lazy val termsConditions: String = host + servicesConfig.getString("urls.footer.termsConditions")
  lazy val govukHelp: String = servicesConfig.getString("urls.footer.govukHelp")
}
