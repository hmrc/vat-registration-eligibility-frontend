/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationConnector @Inject()(val http: HttpClient,
                                         val servicesConfig: ServicesConfig) {
  lazy val vatRegistrationUrl: String = servicesConfig.baseUrl("vat-registration")
  lazy val vatRegistrationUri: String =
    servicesConfig.getConfString("vat-registration.uri", throw new RuntimeException("expected incorporation-information.uri in config but none found"))

  def getRegistrationId()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    http.GET[JsObject](s"$vatRegistrationUrl$vatRegistrationUri/scheme").recover {
      case e => throw logResponse(e, "createNewRegistration")
    }.map {
      json => (json \ "registrationId").as[String]
    }
  }

  def saveEligibility(regId: String, eligibility: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {
    http.PATCH[JsValue, HttpResponse](s"$vatRegistrationUrl$vatRegistrationUri/$regId/eligibility-data", eligibility).map(_.json) recover {
      case e: NotFoundException => logger.error(s"[VatRegistrationConnector][saveEligibility] No vat registration found for regId: $regId")
        throw e
      case e => logger.error(s"[VatRegistrationConnector][saveEligibility] an error occurred for regId: $regId with exception: ${e.getMessage}")
        throw e
    }
  }
}