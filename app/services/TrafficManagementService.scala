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

package services

import config.FrontendAppConfig
import connectors.{Allocated, AllocationResponse, QuotaReached, TrafficManagementConnector}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.{IdGenerator, TimeMachine}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrafficManagementService @Inject()(trafficManagementConnector: TrafficManagementConnector,
                                         val authConnector: AuthConnector,
                                         auditConnector: AuditConnector,
                                         timeMachine: TimeMachine,
                                         idGenerator: IdGenerator
                                        )(implicit ec: ExecutionContext,
                                          appConfig: FrontendAppConfig) extends AuthorisedFunctions {

  def allocate(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[AllocationResponse] =
    authorised().retrieve(Retrievals.credentials) {
      case Some(credentials) =>
        trafficManagementConnector.allocate(regId).map {
          case Allocated =>
            val auditEvent = ExtendedDataEvent(
              auditSource = appConfig.appName,
              auditType = "StartRegistration",
              tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags("start-tax-registration", request.path),
              detail = Json.obj(
                "authProviderId" -> credentials.providerId,
                "journeyId" -> regId
              ),
              generatedAt = timeMachine.instant,
              eventId = idGenerator.createId
            )

            auditConnector.sendExtendedEvent(auditEvent)

            Allocated
          case QuotaReached => QuotaReached //TODO To be finished in the traffic management intergation story
        }
      case None =>
        throw new InternalServerException("[TrafficManagementService][allocate] Missing authProviderId for journey start auditing")
    }
}
