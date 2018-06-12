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

package controllers.helpers

import java.time.LocalDate

import connectors.DataCacheConnector
import models.CurrentProfile
import play.api.mvc.{Request, Result}
import services.{CurrentProfileService, IncorporationInformationService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

trait VATRegistrationControllerHelpers {
  val currentProfileService : CurrentProfileService

  def withCurrentProfile(block: CurrentProfile => Future[Result])(implicit hc : HeaderCarrier): Future[Result] =
    currentProfileService.fetchOrBuildCurrentProfile flatMap block
}
