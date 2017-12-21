/*
 * Copyright 2017 HM Revenue & Customs
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

package connectors

import javax.inject.Inject

import config.WSHttp
import models.external.CompanyRegistrationProfile
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VREFEFeatureSwitches

import scala.concurrent.Future

class CompanyRegistrationConnectorImpl @Inject()(val http: WSHttp,
                                                 val featureSwitch: VREFEFeatureSwitches,
                                                 config: ServicesConfig) extends CompanyRegistrationConnector {
  lazy val companyRegistrationUrl: String = config.baseUrl("company-registration")
  lazy val companyRegistrationUri: String = config.getConfString("company-registration.uri", "")
  lazy val stubUrl: String                = config.baseUrl("incorporation-frontend-stubs")
  lazy val stubUri: String                = config.getConfString("incorporation-frontend-stubs.uri","")
}

trait CompanyRegistrationConnector {
  val companyRegistrationUrl: String
  val companyRegistrationUri: String
  val stubUrl: String
  val stubUri: String

  val http: WSHttp

  val featureSwitch: VREFEFeatureSwitches

  def getCompanyRegistrationDetails(regId: String)(implicit hc : HeaderCarrier) : Future[CompanyRegistrationProfile] = {
    val url = if (useCompanyRegistration) s"$companyRegistrationUrl$companyRegistrationUri/corporation-tax-registration" else s"$stubUrl$stubUri"

    http.GET[JsObject](s"$url/$regId/corporation-tax-registration") map {
      response =>
        val status = (response \ "status").as[String]
        val txId = (response \ "confirmationReferences" \ "transaction-id").as[String]
        CompanyRegistrationProfile(status, txId)
    } recover {
      case badRequestErr: BadRequestException =>
        logger.error(s"[CompanyRegistrationConnect] [getCompanyRegistrationDetails] - Received a BadRequest status code when expecting a Company Registration document for reg id: $regId")
        throw badRequestErr
      case ex: Exception =>
        logger.error(s"[CompanyRegistrationConnect] [getCompanyRegistrationDetails] - Received an error when expecting a Company Registration document for reg id: $regId - error: ${ex.getMessage}")
        throw ex
    }
  }

  private[connectors] def useCompanyRegistration: Boolean = featureSwitch.companyReg.enabled
}



