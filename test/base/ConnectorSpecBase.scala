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

package base

import config.WSHttp
import org.mockito.Matchers
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsValue, Reads}

import scala.concurrent.Future


trait ConnectorSpecBase extends CommonSpecBase with MockitoSugar {
  def mockGet[T](url: String, thenReturn: T) = {
    when(mockWSHttp.GET[Option[T]](Matchers.eq(url))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(thenReturn)))
  }

  def mockGet(url: String, status: Int, body: Option[JsValue] = None) = {
    when(mockWSHttp.GET[HttpResponse](Matchers.eq(url))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, body)))
  }

  def mockFailedGet(url: String, exception: Exception) = {
    when(mockWSHttp.GET[HttpResponse](Matchers.eq(url))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def verifyGetCalled[T](url: String, count: Int = 1) = {
    verify(mockWSHttp, times(count)).GET[Option[T]](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
  }
}
