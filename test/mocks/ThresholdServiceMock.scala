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

package mocks

import models.view.{Threshold, ThresholdView}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import services.ThresholdService

import scala.concurrent.Future

trait ThresholdServiceMock {
this: MockitoSugar =>

  lazy val mockThresholdService = mock[ThresholdService]

  def mockGetThreshold(res: Future[Threshold]):OngoingStubbing[Future[Threshold]] = {
    when(mockThresholdService.getThreshold(any(),any())).thenReturn(res)
  }

  def mockCurrentVatThreshold(res: Future[String]):OngoingStubbing[Future[String]] = {
    when(mockThresholdService.fetchCurrentVatThreshold(any())) thenReturn res
  }

  def mockGetThresholdViewModel[T <: ThresholdView](res: Future[Option[T]]): OngoingStubbing[Future[Option[T]]] ={
    when(mockThresholdService.getThresholdViewModel[T](any(),any(),any())).thenReturn(res)
  }

  def mockSaveThreshold(res:Future[Threshold]):OngoingStubbing[Future[Threshold]] = {
    when(mockThresholdService.saveThreshold(any())(any(),any())).thenReturn(res)
  }

  def mockFetchCurrentVatThreshold(returns: Future[String]) = {
    when(mockThresholdService.fetchCurrentVatThreshold(any()))
      .thenReturn(returns)
  }
}
