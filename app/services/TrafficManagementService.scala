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

package services

import config.{FrontendAppConfig, Logging}
import connectors.{Allocated, AllocationResponse, QuotaReached, TrafficManagementConnector}
import models._
import play.api.libs.json.Json
import play.api.mvc.Request
import services.TrafficManagementService.{charityEnrolment, companyEnrolment, partnershipEnrolment, selfAssesmentEnrolment}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.{IdGenerator, TimeMachine}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off
@Singleton
class TrafficManagementService @Inject()(trafficManagementConnector: TrafficManagementConnector,
                                         val authConnector: AuthConnector,
                                         auditConnector: AuditConnector,
                                         timeMachine: TimeMachine,
                                         idGenerator: IdGenerator
                                        )(implicit ec: ExecutionContext,
                                          appConfig: FrontendAppConfig) extends AuthorisedFunctions with Logging {

  def allocate(regId: String, businessEntity: BusinessEntity)(implicit hc: HeaderCarrier, request: Request[_]): Future[AllocationResponse] =
    authorised().retrieve(Retrievals.credentials and Retrievals.allEnrolments) {
      case Some(credentials) ~ enrolments =>
        val isEnrolled: Boolean = businessEntity match {
          case UKCompany | RegisteredSociety => enrolments.getEnrolment(companyEnrolment).isDefined
          case SoleTrader | NETP => enrolments.getEnrolment(selfAssesmentEnrolment).isDefined
          case Overseas => enrolments.getEnrolment(companyEnrolment).isDefined || enrolments.getEnrolment(selfAssesmentEnrolment).isDefined
          case CharitableIncorporatedOrganisation => enrolments.getEnrolment(charityEnrolment).isDefined
          case _: PartnershipType => enrolments.getEnrolment(partnershipEnrolment).isDefined
          case _ => throw new InternalServerException("[TrafficManagementService][allocate] attempted to allocate for invalid party type")
        }

        trafficManagementConnector.allocate(regId, businessEntity, isEnrolled).map {
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

            logger.info("Started registration journey")

            auditConnector.sendExtendedEvent(auditEvent)

            Allocated
          case QuotaReached =>
            logger.info("Daily quota reached")
            QuotaReached //TODO To be finished in the traffic management intergation story
        }
      case None ~ _ =>
        throw new InternalServerException("[TrafficManagementService][allocate] Missing authProviderId for journey start auditing")
    }

  def getRegistrationInformation()(implicit hc: HeaderCarrier): Future[Option[RegistrationInformation]] =
    trafficManagementConnector.getRegistrationInformation()


  def upsertRegistrationInformation(internalId: String, regId: String, isOtrs: Boolean
                                   )(implicit hc: HeaderCarrier): Future[RegistrationInformation] = {

    val regInfo = RegistrationInformation(
      internalId = internalId,
      registrationId = regId,
      status = Draft,
      regStartDate = Some(LocalDate.now()),
      channel = if (isOtrs) OTRS else VatReg
    )

    trafficManagementConnector.upsertRegistrationInformation(regInfo)
  }
}

object TrafficManagementService {
  val selfAssesmentEnrolment = "IR-SA"
  val partnershipEnrolment = "IR-SA-PART-ORG"
  val companyEnrolment = "IR-CT"
  val charityEnrolment = "HMRC-CHAR-ORG"
}