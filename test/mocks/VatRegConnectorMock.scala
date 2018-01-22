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


import connectors.VatRegistrationConnector
import models.view.Threshold
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait VatRegConnectorMock {
  this: MockitoSugar =>

  lazy val mockRegConnector = mock[VatRegistrationConnector]

  def getThresholdMock(res: Future[Option[JsValue]]): OngoingStubbing[Future[Option[JsValue]]] ={
    when(mockRegConnector.getThreshold(any(),any())).thenReturn(res)
  }

  def patchThresholdMock(res: Future[JsValue]): OngoingStubbing[Future[JsValue]] = {
    when(mockRegConnector.patchThreshold(any())(any(),any())).thenReturn(res)
  }

  def getEligibilityMock(res: Future[Option[(String, Int)]]): OngoingStubbing[Future[Option[(String, Int)]]] = {
    when(mockRegConnector.getEligibility(any(),any())).thenReturn(res)
  }

  def patchEligibilityMock(res: Future[JsValue]): OngoingStubbing[Future[JsValue]] = {
    when(mockRegConnector.patchEligibility(any(), any())(any(),any())).thenReturn(res)
  }
}