/*
 * Copyright 2022 HM Revenue & Customs
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

package mocks

import connectors.AllocationResponse
import models.{BusinessEntity, RegistrationInformation}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import services.TrafficManagementService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait TrafficManagementServiceMock extends MockitoSugar {

  self: Suite =>

  val mockTrafficManagementService: TrafficManagementService = mock[TrafficManagementService]

  def mockServiceAllocation(regId: String, businessEntity: BusinessEntity
                           )(response: Future[AllocationResponse]): OngoingStubbing[Future[AllocationResponse]] =
    when(mockTrafficManagementService.allocate(
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(businessEntity)
    )(ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any())
    ).thenReturn(response)

  def mockGetRegistrationInformation(regId: String)(response: Future[Option[RegistrationInformation]]): OngoingStubbing[Future[Option[RegistrationInformation]]] =
    when(mockTrafficManagementService.getRegistrationInformation(ArgumentMatchers.eq(regId))(ArgumentMatchers.any[HeaderCarrier]))
      .thenReturn(response)

  def mockUpsertRegistrationInformation(internalId: String, regId: String, isOtrs: Boolean)
                                       (response: Future[RegistrationInformation]): OngoingStubbing[Future[RegistrationInformation]] =
    when(mockTrafficManagementService.upsertRegistrationInformation(
      ArgumentMatchers.eq(internalId),
      ArgumentMatchers.eq(regId),
      ArgumentMatchers.eq(isOtrs)
    )(
      ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)
}
