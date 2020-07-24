/*
 * Copyright 2020 HM Revenue & Customs
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
import models.Officer
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VATFeatureSwitch

import scala.concurrent.Future

class IncorporationInformationServiceImpl @Inject()(
                                                     val iiConnector : IncorporationInformationConnector,
                                                     val dataCacheConnector: DataCacheConnector,
                                                     val featureSwitches : VATFeatureSwitch) extends IncorporationInformationService {
}

trait IncorporationInformationService {
  val iiConnector : IncorporationInformationConnector
  val dataCacheConnector: DataCacheConnector
  val featureSwitches: VATFeatureSwitch

  def getIncorpDate(transactionId: String)(implicit hc: HeaderCarrier) : Future[Option[LocalDate]] =
    iiConnector.getIncorpData(transactionId) map {jsOpt =>
      jsOpt map {json =>
        (json \ "incorporationDate").as[LocalDate]
      }
    }

  def getCompanyName(transactionId: String)(implicit hc: HeaderCarrier) : Future[String] =
    iiConnector.getCOHOCompanyDetails(transactionId) map {json =>
        (json \ "company_name").as[String]
      }


  private def officerArrayName = if (featureSwitches.useIiStubbed.enabled) {
    "items"
  } else {
    "officers"
  }

  def getOfficerList(transactionId: String)(implicit hc: HeaderCarrier) : Future[Seq[Officer]] =
    iiConnector.getOfficerList(transactionId) map { json =>
      (json \ officerArrayName).validate[Seq[Officer]](Officer.seqReads)
        .fold(_ => throw new Exception(s"Couldn't get officer list from JSON for txId: $transactionId"), identity)
        .filter{
          officer => officer.resignedOn.isEmpty && (officer.role.equals("director") || officer.role.equals("secretary"))
        } match {
          case Nil        => throw new RuntimeException(s"No eligible officer list found for txId: $transactionId")
          case officers   => officers
        }
    } recover {
      case e => Logger.error(s"[IncorporationInformationService] [getOfficerList] Failed to get officers - ${e.getMessage}")
        throw e
    }
}
