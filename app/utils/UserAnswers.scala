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

package utils

import uk.gov.hmrc.http.cache.client.CacheMap
import identifiers._
import models._
import play.api.libs.json.Reads

class UserAnswers(val cacheMap: CacheMap) extends Enumerable.Implicits {
  def applicationUKNino: Option[Boolean] = cacheMap.getEntry[Boolean](ApplicationUKNinoId.toString)

  def racehorses: Option[Boolean] = cacheMap.getEntry[Boolean](RacehorsesId.toString)

  def agriculturalFlatRateScheme: Option[Boolean] = cacheMap.getEntry[Boolean](AgriculturalFlatRateSchemeId.toString)

  def vATRegistrationException: Option[Boolean] = cacheMap.getEntry[Boolean](VATRegistrationExceptionId.toString)

  def vATExemption: Option[Boolean] = cacheMap.getEntry[Boolean](VATExemptionId.toString)

  def zeroRatedSales: Option[Boolean] = cacheMap.getEntry[Boolean](ZeroRatedSalesId.toString)

  def annualAccountingScheme: Option[Boolean] = cacheMap.getEntry[Boolean](AnnualAccountingSchemeId.toString)

  def internationalActivities: Option[Boolean] = cacheMap.getEntry[Boolean](InternationalActivitiesId.toString)

  def involvedInOtherBusiness: Option[Boolean] = cacheMap.getEntry[Boolean](InvolvedInOtherBusinessId.toString)

  def completionCapacityFillingInFor: Option[CompletionCapacityFillingInFor] = cacheMap.getEntry[CompletionCapacityFillingInFor](CompletionCapacityFillingInForId.toString)

  def completionCapacity: Option[CompletionCapacity] = cacheMap.getEntry[CompletionCapacity](CompletionCapacityId.toString)

  def turnoverEstimate: Option[TurnoverEstimate] = cacheMap.getEntry[TurnoverEstimate](TurnoverEstimateId.toString)

  def thresholdInTwelveMonths: Option[Boolean] = cacheMap.getEntry[Boolean](ThresholdInTwelveMonthsId.toString)

  def voluntaryRegistration: Option[Boolean] = cacheMap.getEntry[Boolean](VoluntaryRegistrationId.toString)

  def thresholdPreviousThirtyDays: Option[Boolean] = cacheMap.getEntry[Boolean](ThresholdPreviousThirtyDaysId.toString)

  def thresholdNextThirtyDays: Option[Boolean] = cacheMap.getEntry[Boolean](ThresholdNextThirtyDaysId.toString)


  def getAnswerBoolean(id: Identifier): Option[Boolean] = {
    cacheMap.getEntry[Boolean](id.toString)
  }
  def getAnswerString(id: Identifier): Option[String] = {
    cacheMap.getEntry[String](id.toString)
  }

}
