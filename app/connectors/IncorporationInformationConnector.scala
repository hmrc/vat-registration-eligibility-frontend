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

import config.{FrontendAppConfig, WSHttp}
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsValue}
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, HttpResponse}

import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class IncorporationInformationConnectorImpl @Inject()(val http : WSHttp, val servicesConfig: FrontendAppConfig) extends IncorporationInformationConnector {
  override val incorpInfoUrl: String = servicesConfig.baseUrl("incorporation-information")
  override val incorpInfoUri: String =
    servicesConfig.getConfString("incorporation-information.uri", throw new RuntimeException("expected incorporation-information.uri in config but none found"))
}
trait IncorporationInformationConnector {
  val http: CoreGet with WSHttp
  val incorpInfoUrl: String
  val incorpInfoUri: String

  def getIncorpData(transactionId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    http.GET[HttpResponse](s"$incorpInfoUrl$incorpInfoUri/incorp-update").map {
      response =>
        if (response.status == 200) Some(response.json) else None
    } recover {
      case e => Logger.error(s"[IncorporationInformationConnector][getIncorpData] an error occurred for txId: $transactionId with exception: ${e.getMessage}")
        throw e
    }
  }

  def getOfficerList(transactionId: String)(implicit hc: HeaderCarrier): Future[Option[Seq[_]]] = ???
}
