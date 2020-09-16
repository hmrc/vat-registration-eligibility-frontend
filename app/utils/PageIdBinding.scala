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

package utils

import identifiers.{Identifier, _}
import models.ConditionalDateFormElement
import uk.gov.hmrc.http.cache.client.CacheMap

object PageIdBinding {
  def sectionBindings(map: CacheMap): Map[String, Seq[(Identifier, Option[Any])]] = {

    val userAnswers = new UserAnswers(map)
    val elemMiss = (e: Identifier) => throw new NoSuchElementException(s"Element missing - $e")
    val illegalState = (e: Identifier) => throw new IllegalStateException(s"Illegal state of elem - $e")
    val twelveMonthsValue = userAnswers.thresholdInTwelveMonths.getOrElse(elemMiss(ThresholdInTwelveMonthsId)).value

    def ThresholdSectionValidationAndConstruction: PartialFunction[(Identifier, Option[Any]), (Identifier, Option[Any])] = {
      case e@(ThresholdNextThirtyDaysId, Some(_)) => if (twelveMonthsValue) {
        illegalState(e._1)
      } else {
        e
      }
      case e@(ThresholdNextThirtyDaysId, None) if !twelveMonthsValue => elemMiss(e._1)
      case e@(VATRegistrationExceptionId, Some(_)) => if (!twelveMonthsValue) {
        illegalState(e._1)
      } else {
        e
      }
      case e@(VATRegistrationExceptionId, None) if (twelveMonthsValue) => illegalState(e._1)
      case e@(VoluntaryRegistrationId, Some(_)) => if (!validateVoluntaryReason) {
        illegalState(e._1)
      } else {
        e
      }
      case e@(VoluntaryRegistrationId, None) if (validateVoluntaryReason) => elemMiss(e._1)
      case e if (e._1 != ThresholdNextThirtyDaysId && e._1 != VATRegistrationExceptionId && e._1 != VoluntaryRegistrationId) => (e._1, e._2.orElse(elemMiss(e._1)))
    }

    def SpecialSituationsValidateAndConstruction: PartialFunction[(Identifier, Option[Any]), (Identifier, Option[Any])] = {
      case e@(VATExemptionId, Some(_)) =>
        if (userAnswers.zeroRatedSales.contains(false)) {
          illegalState(e._1)
        } else {
          e
        }
      case e@(VATExemptionId, None) if (!userAnswers.zeroRatedSales.contains(false)) => elemMiss(e._1)
      case e if (e._1 != VATExemptionId) => (e._1, e._2.orElse(elemMiss(e._1)))
    }

    def validateVoluntaryReason: Boolean = (userAnswers.thresholdNextThirtyDays, userAnswers.thresholdPreviousThirtyDays, userAnswers.thresholdInTwelveMonths) match {
      case (Some(ConditionalDateFormElement(false, _)), Some(ConditionalDateFormElement(false, _)), Some(ConditionalDateFormElement(false, _))) => true
      case _ => false
    }

    Map(
      "VAT-taxable sales" ->
        Seq(
          (ThresholdInTwelveMonthsId, userAnswers.thresholdInTwelveMonths),
          (ThresholdNextThirtyDaysId, userAnswers.thresholdNextThirtyDays),
          (ThresholdPreviousThirtyDaysId, userAnswers.thresholdPreviousThirtyDays),
          (VATRegistrationExceptionId, userAnswers.vatRegistrationException),
          (VoluntaryRegistrationId, userAnswers.voluntaryRegistration),
          (TurnoverEstimateId, userAnswers.turnoverEstimate)
        ).collect(ThresholdSectionValidationAndConstruction),
      "Special situations" ->
        Seq(
          (InternationalActivitiesId, userAnswers.internationalActivities),
          (InvolvedInOtherBusinessId, userAnswers.involvedInOtherBusiness),
          (AnnualAccountingSchemeId, userAnswers.annualAccountingScheme),
          (ZeroRatedSalesId, userAnswers.zeroRatedSales),
          (RegisteringBusinessId, userAnswers.registeringBusiness),
          (NinoId, userAnswers.nino),
          (VATExemptionId, userAnswers.vatExemption),
          (AgriculturalFlatRateSchemeId, userAnswers.agriculturalFlatRateScheme),
          (RacehorsesId, userAnswers.racehorses)
        ).collect(SpecialSituationsValidateAndConstruction)
    )
  }
}