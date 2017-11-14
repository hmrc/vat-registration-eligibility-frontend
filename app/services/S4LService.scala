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

package services

import javax.inject.{Inject, Singleton}

import connectors.S4LConnector
import models.{CurrentProfile, S4LKey, ViewModelFormat}
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

@Singleton
class S4LService @Inject()(val s4LConnector: S4LConnector) {

  def save[T: S4LKey](data: T)(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[CacheMap] =
    s4LConnector.save[T](profile.registrationId, S4LKey[T].key, data)

  def updateViewModel[T, G](data: T, container: Future[G])
                           (implicit hc: HeaderCarrier,
                            profile: CurrentProfile,
                            vmf: ViewModelFormat.Aux[T, G],
                            f: Format[G],
                            k: S4LKey[G]): Future[CacheMap] =
    for {
      group <- container
      cm <- s4LConnector.save(profile.registrationId, k.key, vmf.update(data, Some(group)))
    } yield cm

  def fetchAndGet[T: S4LKey]()(implicit profile: CurrentProfile, hc: HeaderCarrier, format: Format[T]): Future[Option[T]] =
    s4LConnector.fetchAndGet[T](profile.registrationId, S4LKey[T].key).value

  def getViewModel[T, G](container: Future[G])
                        (implicit r: ViewModelFormat.Aux[T, G], f: Format[G]): Future[Option[T]] =
    container.map(r.read)

  def clear()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[HttpResponse] =
    s4LConnector.clear(profile.registrationId)

  def fetchAll()(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Option[CacheMap]] =
    s4LConnector.fetchAll(profile.registrationId)

}
