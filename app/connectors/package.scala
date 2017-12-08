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

import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status
import uk.gov.hmrc.http.{BadRequestException, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}

import scala.language.implicitConversions

package object connectors {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def logResponse(e: Throwable, func: String): Throwable = {
    e match {
      case e: NotFoundException   => logger.warn(s"[$func] received NOT FOUND")
      case e: BadRequestException => logger.warn(s"[$func] received BAD REQUEST")
      case e: Upstream4xxResponse => e.upstreamResponseCode match {
        case Status.FORBIDDEN => logger.error(s"[$func] received FORBIDDEN")
        case _                => logger.error(s"[$func] received Upstream 4xx: ${e.upstreamResponseCode} ${e.message}")
      }
      case e: Upstream5xxResponse => logger.error(s"[$func] received Upstream 5xx: ${e.upstreamResponseCode}")
      case e: Exception           => logger.error(s"[$func] received ERROR: ${e.getMessage}")
    }
    e
  }
}

