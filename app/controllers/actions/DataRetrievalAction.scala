/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import models.requests.{CacheIdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}


class DataRetrievalActionImpl @Inject()(val sessionService: SessionService)
                                       (implicit val executionContext: ExecutionContext) extends DataRetrievalAction {

  override protected def transform[A](request: CacheIdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    sessionService.fetch.map {
      case None =>
        OptionalDataRequest(request.request, request.regId, request.internalId, None)
      case Some(data) =>
        OptionalDataRequest(request.request, request.regId, request.internalId, Some(new UserAnswers(data)))
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[CacheIdentifierRequest, OptionalDataRequest]
