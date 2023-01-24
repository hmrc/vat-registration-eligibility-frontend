/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.VatRegistrationConnector
import models.requests.DataRequest
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import utils.PageIdBinding

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationService @Inject()(val vrConnector: VatRegistrationConnector,
                                       val sessionService: SessionService) {

  def submitEligibility(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[_]): Future[JsObject] = {
    for {
      cacheMap <- sessionService.fetch.map(_.getOrElse(throw new InternalServerException("Missing answer cachemap on submission to VRS")))
      _ = PageIdBinding.sectionBindings(cacheMap) // Originally was used to prepare data for submission, currently only purpose is to bounce people with impossible answers back
      json = Json.toJson(cacheMap.data).as[JsObject]
      _ <- vrConnector.saveEligibility(request.regId, json)
    } yield json
  }

}
