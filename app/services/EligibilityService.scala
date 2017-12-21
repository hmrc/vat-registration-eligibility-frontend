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

import common.enums.{CacheKeys, EligibilityResult}
import connectors.{S4LConnector, VatRegistrationConnector}
import models.CurrentProfile
import models.view.{Eligibility, YesOrNoQuestion}
import transformers.ToEligibilityView
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

case class InvalidVersionException(msg: String) extends Exception(msg)

class EligibilityServiceImpl @Inject()(val s4LConnector: S4LConnector,
                                       val vatRegistrationConnector: VatRegistrationConnector,
                                       config: ServicesConfig) extends EligibilityService {
  override val currentVersion = config.getInt("currentVersion")
}

trait EligibilityService {
  val s4LConnector: S4LConnector
  val vatRegistrationConnector: VatRegistrationConnector

  val currentVersion: Int

  private val T = Some(true)
  private val F = Some(false)
  private val N = None

  private def fromAPI(apiResult: (String, Int)): Eligibility = {
    val (result, version): (String, Int) = apiResult
    version match {
      case 1 => ToEligibilityView.fromAPIVersion1(result)
      case _ => throw InvalidVersionException(s"Invalid eligibility questions version; found version $version")
    }
  }

  def getEligibility(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Eligibility] = {
    def getFromApi: Future[Eligibility] = vatRegistrationConnector.getEligibility flatMap  {
      _.fold(Future.successful(Eligibility(N,N,N,N,N,N))) { api =>
        val view = fromAPI(api)
        s4LConnector.save(cp.registrationId, CacheKeys.Eligibility, view) map(_ => view)
      }
    }

    s4LConnector.fetchAndGet[Eligibility](cp.registrationId, CacheKeys.Eligibility) flatMap {
      _.fold(getFromApi)(Future.successful)
    }
  }

  private def updateEligibility(result: String, version: Int)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[(String, Int)] =
    vatRegistrationConnector.patchEligibility(result, currentVersion) flatMap { _ =>
      s4LConnector.clear(cp.registrationId) map {
        _ => (result, version)
      }
    }

  private def updateModel(answeredQuestion: YesOrNoQuestion, eligibility: Eligibility): Eligibility = {
    import common.enums.{EligibilityQuestions => q}

    q.withName(answeredQuestion.question) match {
      case q.`haveNino`                => eligibility.copy(hasNino = Some(answeredQuestion.answer))
      case q.`doingBusinessAbroad`     => eligibility.copy(internationalBusiness = Some(answeredQuestion.answer))
      case q.`doAnyApplyToYou`         => eligibility.copy(moreThanOneBusinessOrLegalStatus = Some(answeredQuestion.answer))
      case q.`applyingForAnyOf`        => eligibility.copy(agriculturalFRSOrAnnualAccount = Some(answeredQuestion.answer))
      case q.`applyingForVatExemption` => eligibility.copy(exceptionOrExemption = Some(answeredQuestion.answer))
      case q.`companyWillDoAnyOf`      => eligibility.copy(racehorsesOrLandAndProperty = Some(answeredQuestion.answer))
    }
  }

  private def isModelComplete(eligibility: Eligibility, version: Int): Completion[String] = {
    val questionResults = EligibilityResult(version).questions
    eligibility match {
      case Eligibility(F,_,_,_,_,_) => Completed(questionResults.noNino)
      case Eligibility(_,T,_,_,_,_) => Completed(questionResults.doingInternationalBusiness)
      case Eligibility(_,_,T,_,_,_) => Completed(questionResults.otherInvolvementsOrCOLE)
      case Eligibility(_,_,_,T,_,_) => Completed(questionResults.wantsAFRSOrAAS)
      case Eligibility(_,_,_,_,T,_) => Completed(questionResults.wantsExemption)
      case Eligibility(_,_,_,_,_,T) => Completed(questionResults.racehorsesOrLandAndProperty)
      case Eligibility(T,F,F,F,F,F) => Completed(questionResults.success)
      case _                        => Incomplete("incomplete")
    }
  }

  def saveEligibility(answeredQuestion: YesOrNoQuestion)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Eligibility] ={
    for {
      before <- getEligibility
      update = updateModel(answeredQuestion, before)
      _      <- isModelComplete(update, currentVersion).fold(
        incomplete => s4LConnector.save(cp.registrationId, CacheKeys.Eligibility, update),
        complete  => updateEligibility(complete, currentVersion)
      )
    } yield update
  }
}
