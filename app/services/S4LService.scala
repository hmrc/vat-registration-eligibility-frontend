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

package services

import connectors._
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class S4LService @Inject()(val s4LConnector: S4LConnector) {

  def fetchAndGet[T](id: String, key: String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](id, key)

  def save[T](id: String, key: String, data: T)(implicit hc: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    s4LConnector.save[T](id, key, data)

  def clear(id: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    s4LConnector.clear(id)

}
