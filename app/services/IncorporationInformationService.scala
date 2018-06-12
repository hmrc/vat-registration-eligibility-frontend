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

package services

import java.time.LocalDate

import connectors.{DataCacheConnector, IncorporationInformationConnector}
import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class IncorporationInformationServiceImpl @Inject()(
                                                     val iiConnector : IncorporationInformationConnector,
                                                     val dataCacheConnector: DataCacheConnector) extends IncorporationInformationService {
}

trait IncorporationInformationService {
  val iiConnector : IncorporationInformationConnector
  val dataCacheConnector: DataCacheConnector

  def retrieveIncorporationDate(transactionId : String)(implicit hc : HeaderCarrier) : Future[Option[LocalDate]] = {
    dataCacheConnector.getEntry[LocalDate](hc.sessionId.get.value, "IncorporationDate").flatMap(
      _.fold(
        for {
          incorpDate <- getIncorpDateFromII(transactionId)
          _          <- saveIncorpDateToDataCache(incorpDate)
        } yield incorpDate
      )( x => Future.successful(Some(x)))
    )
  }

  def getIncorpDateFromII(transactionId: String)(implicit hc: HeaderCarrier) : Future[Option[LocalDate]] =
    iiConnector.getIncorpData(transactionId) map {jsOpt =>
      jsOpt map {json =>
        (json \ "incorporationDate").as[LocalDate]
      }
    }

  def saveIncorpDateToDataCache(incorpDate: Option[LocalDate])(implicit hc: HeaderCarrier) : Future[Option[LocalDate]] = incorpDate match {
    case Some(date) => dataCacheConnector.save[LocalDate](hc.sessionId.get.value, "IncorporationDate", date).map(_ => incorpDate)
    case _ => Future.successful(None)
  }


}
