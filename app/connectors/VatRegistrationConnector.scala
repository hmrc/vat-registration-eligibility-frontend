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

package connectors

import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationConnector @Inject()(val http: HttpClientV2,
                                         val servicesConfig: ServicesConfig) extends LoggingUtil {
  lazy val vatRegistrationUrl: String = servicesConfig.baseUrl("vat-registration")
  lazy val vatRegistrationUri: String =
    servicesConfig.getConfString("vat-registration.uri", throw new RuntimeException("expected incorporation-information.uri in config but none found"))

  def saveEligibility(regId: String, eligibility: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Future[JsValue] = {
    val url = s"$vatRegistrationUrl$vatRegistrationUri/$regId/eligibility-data"
    http.patch(url"$url")
      .withBody(eligibility)
      .execute[HttpResponse]
      .map(_.json)
      .recover {
        case e: NotFoundException => errorLog(s"[VatRegistrationConnector][saveEligibility] No vat registration found for regId: $regId")
          throw e
        case e => errorLog(s"[VatRegistrationConnector][saveEligibility] an error occurred for regId: $regId with exception: ${e.getMessage}")
          throw e
      }
  }

  def getEligibilityAnswers(regId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Map[String, JsValue]]] = {
    val url = s"$vatRegistrationUrl$vatRegistrationUri/registrations/$regId/sections/eligibilityJson"
    http.get(url"$url")
      .execute[Option[Map[String, JsValue]]]
  }
}