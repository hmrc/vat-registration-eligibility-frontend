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
import models.external.BusinessProfile
import play.api.http.Status.FORBIDDEN
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.inject.ServicesConfig

import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Future

class BusinessRegistrationConnectorImpl @Inject()(val http: WSHttp, config: ServicesConfig) extends BusinessRegistrationConnector {
  lazy val businessRegUrl = config.baseUrl("business-registration")
}

trait BusinessRegistrationConnector {
  val businessRegUrl: String

  val http: WSHttp

  def retrieveBusinessProfile(implicit hc: HeaderCarrier, rds: HttpReads[BusinessProfile]): Future[BusinessProfile] = {
    http.GET[BusinessProfile](s"$businessRegUrl/business-registration/business-tax-registration") recover {
      case e => throw logResponse(e, "retrieveBusinessProfile", "retrieving business profile")
    }
  }

  private[connectors] def logResponse(e: Throwable, f: String, m: String, regId: Option[String] = None): Throwable = {
    val optRegId = regId.fold("")(id => s" and regId: $id")
    def log(s: String): Unit = logger.error(s"[BusinessRegistrationConnector] [$f] received $s when $m$optRegId")
    e match {
      case e: NotFoundException   => log("NOT FOUND")
      case e: BadRequestException => log("BAD REQUEST")
      case e: Upstream4xxResponse => e.upstreamResponseCode match {
        case FORBIDDEN => log("FORBIDDEN")
        case _         => log(s"Upstream 4xx: ${e.upstreamResponseCode} ${e.message}")
      }
      case e: Upstream5xxResponse => log(s"Upstream 5xx: ${e.upstreamResponseCode}")
      case e: Exception           => log(s"ERROR: ${e.getMessage}")
    }
    e
  }
}
