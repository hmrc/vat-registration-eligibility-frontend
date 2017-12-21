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

package mocks

import models.view.Eligibility
import org.scalatest.mockito.MockitoSugar
import services.EligibilityService
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.Future
trait EligibilityServiceMock {
  this: MockitoSugar =>

  lazy val mockEligibilityService = mock[EligibilityService]

  def mockGetEligibility(res: Future[Eligibility]):OngoingStubbing[Future[Eligibility]] = {
    when(mockEligibilityService.getEligibility(any(),any())).thenReturn(res)
  }

  def mockSaveEligibility(res:Future[Eligibility]):OngoingStubbing[Future[Eligibility]] ={
    when(mockEligibilityService.saveEligibility(any())(any(),any())).thenReturn(res)
  }
}
