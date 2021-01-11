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

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.RegistrationInformation
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.http.Status._
import play.api.libs.json.{Json, Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrafficManagementConnector @Inject()(httpClient: HttpClient,
                                           config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def allocate(regId: String)(implicit hc: HeaderCarrier): Future[AllocationResponse] =
    httpClient.POSTEmpty[HttpResponse](config.trafficAllocationUrl(regId)).map {
      _.status match {
        case CREATED =>
          Allocated
        case _ =>
          throw new InternalServerException("[TrafficManagementConnector][allocate] Unexpected response from VAT Registration")
      }
    }.recover {
      case Upstream4xxResponse(_, TOO_MANY_REQUESTS, _, _) => QuotaReached
    }

  def getRegistrationInformation()(implicit hc: HeaderCarrier): Future[Option[RegistrationInformation]] =
    httpClient.GET[Option[RegistrationInformation]](config.getRegistrationInformationUrl)

  def upsertRegistrationInformation[DataType](regInfo: DataType
                                             )(implicit hc: HeaderCarrier, dataTypeWriter: Writes[DataType]): Future[RegistrationInformation] =
    httpClient.PUT[DataType, RegistrationInformation](config.upsertRegistrationInformationUrl, regInfo)

}

sealed trait AllocationResponse

case object Allocated extends AllocationResponse

case object QuotaReached extends AllocationResponse