/*
 * Copyright 2018 HM Revenue & Customs
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

import config.WSHttp
import javax.inject.Inject
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VATFeatureSwitch

import scala.concurrent.Future

class CompanyRegistrationConnectorImpl @Inject()(val http: WSHttp,
                                                 val config: ServicesConfig,
                                                 val featureSwitch: VATFeatureSwitch) extends CompanyRegistrationConnector {
  val companyRegistrationUrl: String = config.baseUrl("company-registration")
  val stubUrl: String = config.baseUrl("incorporation-frontend-stub")
  def useStub: Boolean = featureSwitch.useCrStubbed.enabled
}

trait CompanyRegistrationConnector {
  val companyRegistrationUrl: String
  val stubUrl: String
  val http: WSHttp
  def useStub: Boolean

  def getTransactionId(regId: String)(implicit hc: HeaderCarrier): Future[String] = {
    val url = if(useStub) {
      s"$stubUrl/incorporation-frontend-stub"
    } else {
      s"$companyRegistrationUrl/company-registration/corporation-tax-registration"
    }

    http.GET[JsValue](s"$url/$regId/corporation-tax-registration") map {
      _.\("confirmationReferences").\("transaction-id").as[String]
    } recover {
      case e => throw logResponse(e, "getTransactionId")
    }
  }
}