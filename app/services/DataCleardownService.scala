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

import identifiers._
import models.requests.DataRequest
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataCleardownService @Inject()(sessionService: SessionService)
                                    (implicit executionContext: ExecutionContext) {

  def cleardownOnAnswerChange[T](newAnswer: T, identifier: Identifier)
                                (implicit request: DataRequest[AnyContent], headerCarrier: HeaderCarrier, reads: Reads[T]): Future[CacheMap] =
    (request.userAnswers.getAnswer[T](identifier), identifier) match {
      case (Some(oldAnswer), RegistrationReasonId | FixedEstablishmentId) if oldAnswer != newAnswer => clearAllThresholdData
      case (None | Some(_), _) => Future.successful(request.userAnswers.cacheMap)
    }

  private def clearAllThresholdData(implicit headerCarrier: HeaderCarrier): Future[CacheMap] =
    for {
      _ <- sessionService.removeEntry(ThresholdInTwelveMonthsId.toString)
      _ <- sessionService.removeEntry(ThresholdPreviousThirtyDaysId.toString)
      _ <- sessionService.removeEntry(ThresholdNextThirtyDaysId.toString)
      _ <- sessionService.removeEntry(VoluntaryRegistrationId.toString)
      _ <- sessionService.removeEntry(DateOfBusinessTransferId.toString)
      _ <- sessionService.removeEntry(PreviousBusinessNameId.toString)
      _ <- sessionService.removeEntry(VATNumberId.toString)
      _ <- sessionService.removeEntry(KeepOldVrnId.toString)
      _ <- sessionService.removeEntry(TermsAndConditionsId.toString)
      _ <- sessionService.removeEntry(TaxableSuppliesInUkId.toString)
      cacheMap <- sessionService.removeEntry(ThresholdTaxableSuppliesId.toString)
    } yield cacheMap

}
