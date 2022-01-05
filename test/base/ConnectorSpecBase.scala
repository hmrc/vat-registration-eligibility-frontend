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

package base

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future


trait ConnectorSpecBase extends CommonSpecBase {
  def mockGet[T](url: String, thenReturn: T) = {
    when(mockHttpClient.GET[T](ArgumentMatchers.eq(url))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockPost[T, O](url: String, thenReturn: O) = {
    when(mockHttpClient.POST[T, O](ArgumentMatchers.eq(url), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(thenReturn))
  }

  def mockGet(url: String, status: Int, body: Option[JsValue] = None) = {
    when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.eq(url))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, body)))
  }

  def mockPost[T](url: String, status: Int, body: Option[JsValue] = None) = {
    when(mockHttpClient.POST[T, HttpResponse](ArgumentMatchers.eq(url), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, body)))
  }

  def mockPatch[T](url: String, status: Int, body: Option[JsValue] = None) = {
    when(mockHttpClient.PATCH[T, HttpResponse](ArgumentMatchers.eq(url), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(status, body)))
  }

  def mockFailedGet(url: String, exception: Exception) = {
    when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.eq(url))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.failed(exception))
  }

  def verifyGetCalled[T](url: String, count: Int = 1) = {
    verify(mockHttpClient, times(count)).GET[Option[T]](ArgumentMatchers.eq(url))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  def verifyPostCalled[T, O](url: String, count: Int = 1) = {
    verify(mockHttpClient, times(count)).POST[Option[T], O](ArgumentMatchers.eq(url), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  def verifyPatchCalled[T, O](url: String, count: Int = 1) = {
    verify(mockHttpClient, times(count)).PATCH[Option[T], O](ArgumentMatchers.eq(url), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
  }
}
