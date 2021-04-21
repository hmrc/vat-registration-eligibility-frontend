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

package mocks

import models.{CurrentProfile, S4LKey}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.mockito.{ArgumentMatchers => Matchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Format
import play.api.test.Helpers._
import services.S4LService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

trait MockS4LService {
  this: MockitoSugar =>

  lazy val mockS4LService: S4LService = mock[S4LService]

  def mockS4LFetchAndGet[T](regId: String, key: String)(response: Option[T]): OngoingStubbing[Future[Option[T]]] =
    when(mockS4LService.fetchAndGet[T](
      Matchers.eq(key)
    )(
      Matchers.eq(CurrentProfile(regId)),
      Matchers.any[HeaderCarrier](),
      Matchers.any[Format[T]]())
    ).thenReturn(Future.successful(response))

  def mockS4LClear(regId: String): OngoingStubbing[Future[HttpResponse]] =
    when(mockS4LService.clear(
      Matchers.any[HeaderCarrier](),
      Matchers.eq(CurrentProfile(regId)))
    ).thenReturn(Future.successful(HttpResponse(OK, "")))

  def mockS4LSave[T](regId: String, key: String, data: T)(response: Future[CacheMap]): OngoingStubbing[Future[CacheMap]] =
    when(mockS4LService.save[T](
      Matchers.contains(key),
      Matchers.eq[T](data)
    )(
      Matchers.eq(CurrentProfile(regId)),
      Matchers.any[HeaderCarrier](),
      Matchers.any[Format[T]]())
    ).thenReturn(response)

  def mockS4LSave[T: S4LKey](regId: String, data: T)(response: Future[CacheMap]): OngoingStubbing[Future[CacheMap]] =
    when(mockS4LService.save[T](
      Matchers.eq[T](data)
    )(
      Matchers.any[S4LKey[T]],
      Matchers.eq(CurrentProfile(regId)),
      Matchers.any[HeaderCarrier](),
      Matchers.any[Format[T]]())
    ).thenReturn(response)

}
