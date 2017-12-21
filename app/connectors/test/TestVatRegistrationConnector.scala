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

package connectors.test

import javax.inject.Inject

import config.WSHttp
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class TestVatRegistrationConnectorImpl @Inject()(val http: WSHttp, config: ServicesConfig) extends TestVatRegistrationConnector {
  lazy val vatRegUrl = config.baseUrl("vat-registration")
}

trait TestVatRegistrationConnector {
  val vatRegUrl: String

  val http: WSHttp

  def dropCollection(implicit hc: HeaderCarrier): Future[Result] = {
    http.POSTEmpty[HttpResponse](s"$vatRegUrl/vatreg/test-only/clear").map(_ => Ok)
  }
}
