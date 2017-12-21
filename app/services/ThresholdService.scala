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

import javax.inject.Inject

import common.enums.CacheKeys
import connectors.{S4LConnector, VatRegistrationConnector}
import models.CurrentProfile
import models.view.TaxableTurnover._
import models.view.VoluntaryRegistration._
import models.view._
import transformers.ToThresholdView
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class ThresholdServiceImpl @Inject()(val s4LConnector: S4LConnector,
                                     val vatRegistrationConnector: VatRegistrationConnector) extends ThresholdService

trait ThresholdService {
  val s4LConnector: S4LConnector
  val vatRegistrationConnector: VatRegistrationConnector

  private def updateThreshold(data: Threshold)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Threshold] =
    vatRegistrationConnector.patchThreshold(data) flatMap { _ =>
      s4LConnector.clear(cp.registrationId) map {
        _ => data
      }
    }

  private def updateVoluntaryInfo(threshold: Threshold): Threshold = threshold match {
    case Threshold(Some(TaxableTurnover(TAXABLE_YES)), _, _, _, _) =>
      threshold.copy(voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_NO)), voluntaryRegistrationReason = None)
    case Threshold(None, _, _, Some(OverThresholdView(s1, _)), Some(ExpectationOverThresholdView(s2, _))) if s1 || s2 =>
      threshold.copy(voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_NO)), voluntaryRegistrationReason = None)
    case Threshold(_, Some(VoluntaryRegistration(REGISTER_NO)), Some(_), _, _) =>
      threshold.copy(voluntaryRegistrationReason = None)
    case _ => threshold
  }

  private def updateModel(view: ThresholdView, threshold: Threshold): Threshold = {
    val model = view match {
      case a: TaxableTurnover => threshold.copy(taxableTurnover = Some(a))
      case b: OverThresholdView => threshold.copy(overThreshold = Some(b))
      case c: ExpectationOverThresholdView => threshold.copy(expectationOverThreshold = Some(c))
      case d: VoluntaryRegistration => threshold.copy(voluntaryRegistration = Some(d))
      case e: VoluntaryRegistrationReason => threshold.copy(voluntaryRegistrationReason = Some(e))
    }

    updateVoluntaryInfo(model)
  }

  private def isModelComplete(threshold: Threshold)(implicit cp: CurrentProfile): Completion[Threshold] = threshold match {
    case Threshold(
      Some(TaxableTurnover(TAXABLE_NO)),
      Some(VoluntaryRegistration(REGISTER_YES)),
      Some(VoluntaryRegistrationReason(_)),
      None,
      None) => Completed(threshold)
    case Threshold(
      Some(TaxableTurnover(TAXABLE_YES)),
      Some(VoluntaryRegistration(REGISTER_NO)),
      None,
      None,
      None) => Completed(threshold)
    case Threshold(
      None,
      Some(VoluntaryRegistration(REGISTER_YES)),
      Some(VoluntaryRegistrationReason(_)),
      Some(OverThresholdView(false, _)),
      Some(ExpectationOverThresholdView(false, _))) => Completed(threshold)
    case Threshold(
      None,
      Some(VoluntaryRegistration(REGISTER_NO)),
      None,
      Some(OverThresholdView(false, _)),
      Some(ExpectationOverThresholdView(false, _))) => Incomplete(threshold)
    case Threshold(
      None,
      Some(VoluntaryRegistration(REGISTER_NO)),
      None,
      Some(OverThresholdView(_, _)),
      Some(ExpectationOverThresholdView(_, _))) => Completed(threshold)
    case _ => Incomplete(threshold)
  }

  def saveThreshold(view: ThresholdView)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Threshold] = {
    getThreshold flatMap { threshold =>
      isModelComplete(updateModel(view, threshold)).fold(
        incomplete => s4LConnector.save(cp.registrationId, CacheKeys.Threshold, incomplete).map(_ => incomplete),
        complete => updateThreshold(complete)
      )
    }
  }

  private def viewModelConvert[T <: ThresholdView](threshold: Threshold)(implicit f: Threshold => Option[T]): Option[T] = f(threshold)

  def getThresholdViewModel[T <: ThresholdView](implicit cp: CurrentProfile, hc: HeaderCarrier, f: Threshold => Option[T]): Future[Option[T]] =
    getThreshold map viewModelConvert[T]

  def getThreshold(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Threshold] = {
    def getFromApi: Future[Threshold] = vatRegistrationConnector.getThreshold flatMap {
      _.fold(Future.successful(Threshold(None, None, None, None, None))) { jsonApi =>
        val threshold = ToThresholdView.fromAPI(jsonApi, cp.incorporationDate.isDefined)
        s4LConnector.save(cp.registrationId, CacheKeys.Threshold, threshold).map(_ => threshold)
      }
    }

    s4LConnector.fetchAndGet[Threshold](cp.registrationId, CacheKeys.Threshold).flatMap(
      _.fold(getFromApi)(a => Future.successful(a)))
  }
}