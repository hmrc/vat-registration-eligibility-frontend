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

import base.SpecBase
import connectors.{Allocated, QuotaReached}
import mocks.TrafficManagementConnectorMock
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.Json
import play.api.mvc.Request
import services.TrafficManagementService.{charityEnrolment, companyEnrolment, selfAssesmentEnrolment, trustEnrolment, unincorporatedAssociationEnrolment}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import utils.{FakeIdGenerator, FakeTimeMachine}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class TrafficManagementServiceSpec extends SpecBase
  with TrafficManagementConnectorMock {

  val timeMachine = new FakeTimeMachine
  val idGenerator = new FakeIdGenerator

  object Service extends TrafficManagementService(
    mockTrafficManagementConnector,
    mockAuthConnector,
    mockAuditConnector,
    timeMachine,
    idGenerator
  )

  val testInternalId = "testInternalId"
  val testRegId = "testRegId"
  val testProviderId: String = "testProviderID"
  val testProviderType: String = "GovernmentGateway"
  val testCredentials: Credentials = Credentials(testProviderId, testProviderType)
  val testDate = LocalDate.now

  implicit val request: Request[_] = fakeRequest
  val testEnrolments: Enrolments = Enrolments(Set(
    Enrolment(companyEnrolment),
    Enrolment(selfAssesmentEnrolment),
    Enrolment(charityEnrolment),
    Enrolment(trustEnrolment),
    Enrolment(unincorporatedAssociationEnrolment)
  ))

  "allocate" must {
    Seq(
      UKCompany,
      RegisteredSociety,
      SoleTrader,
      NETP,
      Overseas,
      CharitableIncorporatedOrganisation,
      NonIncorporatedTrust,
      UnincorporatedAssociation
    ).foreach { partyType =>
      s"pass through the value when the connector returns an Allocated response for an enrolled ${partyType.toString}" in {
        mockAllocation(testRegId, partyType, isEnrolled = true)(Future.successful(Allocated))
        when(
          mockAuthConnector.authorise(
            ArgumentMatchers.any,
            ArgumentMatchers.eq(Retrievals.credentials and Retrievals.allEnrolments)
          )(
            ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext]
          )
        ).thenReturn(Future.successful(new ~(Some(testCredentials), testEnrolments)))

        val testAuditEvent = ExtendedDataEvent(
          auditSource = frontendAppConfig.appName,
          auditType = "StartRegistration",
          tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags("start-tax-registration", request.path),
          detail = Json.obj(
            "authProviderId" -> testProviderId,
            "journeyId" -> testRegId
          ),
          generatedAt = timeMachine.instant,
          eventId = idGenerator.createId
        )

        val res = await(Service.allocate(testRegId, partyType))

        res mustBe Allocated

        verify(mockAuditConnector).sendExtendedEvent(
          ArgumentMatchers.eq(testAuditEvent)
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext]
        )
      }

      s"pass through the value when the connector returns an Allocated response for an unenrolled ${partyType.toString}" in {
        mockAllocation(testRegId, partyType, isEnrolled = false)(Future.successful(Allocated))
        when(
          mockAuthConnector.authorise(
            ArgumentMatchers.any,
            ArgumentMatchers.eq(Retrievals.credentials and Retrievals.allEnrolments)
          )(
            ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext]
          )
        ).thenReturn(Future.successful(new ~(Some(testCredentials), Enrolments(Set()))))

        val testAuditEvent = ExtendedDataEvent(
          auditSource = frontendAppConfig.appName,
          auditType = "StartRegistration",
          tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags("start-tax-registration", request.path),
          detail = Json.obj(
            "authProviderId" -> testProviderId,
            "journeyId" -> testRegId
          ),
          generatedAt = timeMachine.instant,
          eventId = idGenerator.createId
        )

        val res = await(Service.allocate(testRegId, partyType))

        res mustBe Allocated

        verify(mockAuditConnector).sendExtendedEvent(
          ArgumentMatchers.eq(testAuditEvent)
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext]
        )
      }
    }

    "pass through the value when the connector returns an QuotaReached response" in {
      mockAllocation(testRegId, UKCompany, isEnrolled = true)(Future.successful(QuotaReached))
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.any,
          ArgumentMatchers.eq(Retrievals.credentials and Retrievals.allEnrolments)
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext]
        )
      ).thenReturn(Future.successful(new ~(Some(testCredentials), testEnrolments)))

      val res = await(Service.allocate(testRegId, UKCompany))

      res mustBe QuotaReached
    }
  }

  "getRegistrationInformation" must {
    "return registration information if registration information exists" in {
      val testRegInfo = RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)
      mockGetRegistrationInformation()(Future.successful(Some(testRegInfo)))

      val res = await(Service.getRegistrationInformation())

      res mustBe Some(testRegInfo)
    }
    "return None if no registration information exists" in {
      mockGetRegistrationInformation()(Future.successful(None))

      val res = await(Service.getRegistrationInformation())

      res mustBe None
    }
  }
  "upsertRegistrationInformation" must {
    "return registration information" in {
      val testRegInfo = RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)
      mockUpsertRegistrationInformation(testRegInfo)(Future.successful(testRegInfo))

      val res = await(Service.upsertRegistrationInformation(testInternalId, testRegId, false))

      res mustBe testRegInfo
    }
  }

}
