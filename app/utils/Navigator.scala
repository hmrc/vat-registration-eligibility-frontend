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

import controllers.routes
import identifiers._
import javax.inject.{Inject, Singleton}
import models.Mode
import play.api.mvc.Call

@Singleton
class Navigator @Inject()() {

  private[utils] def pageIdToPageLoad(pageId: Identifier): Call = pageId match {
    case ThresholdNextThirtyDaysId => routes.ThresholdNextThirtyDaysController.onPageLoad()
    case ThresholdPreviousThirtyDaysId => routes.ThresholdPreviousThirtyDaysController.onPageLoad()
    case VoluntaryRegistrationId => routes.VoluntaryRegistrationController.onPageLoad()
    case ChoseNotToRegisterId => routes.ChoseNotToRegisterController.onPageLoad()
    case ThresholdInTwelveMonthsId => routes.ThresholdInTwelveMonthsController.onPageLoad()
    case TurnoverEstimateId => routes.TurnoverEstimateController.onPageLoad()
    case CompletionCapacityId => routes.CompletionCapacityController.onPageLoad()
    case CompletionCapacityFillingInForId => routes.CompletionCapacityFillingInForController.onPageLoad()
    case InvolvedInOtherBusinessId => routes.InvolvedInOtherBusinessController.onPageLoad()
    case EligibilityDropoutId => routes.EligibilityDropoutController.onPageLoad()
    case InternationalActivitiesId => routes.InternationalActivitiesController.onPageLoad()
    case AnnualAccountingSchemeId => routes.AnnualAccountingSchemeController.onPageLoad()
    case ZeroRatedSalesId => routes.ZeroRatedSalesController.onPageLoad()
    case VATExemptionId => routes.VATExemptionController.onPageLoad()
    case VATRegistrationExceptionId => routes.VATRegistrationExceptionController.onPageLoad()
    case ApplyInWritingId => routes.ApplyInWritingController.onPageLoad()
    case AgriculturalFlatRateSchemeId => routes.AgriculturalFlatRateSchemeController.onPageLoad()
    case RacehorsesId => routes.RacehorsesController.onPageLoad()
    case ApplicationUKNinoId => routes.ApplicationUKNinoController.onPageLoad()
    case _ => throw new RuntimeException(s"[Navigator] [pageIdToPageLoad] Could not load page for pageId: $pageId")
  }

  private[utils] def nextOnFalse(fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswerBoolean(fromPage) match {
        case Some(false) => pageIdToPageLoad(toPage)
        case _ => pageIdToPageLoad(toPage)
      }
    }
  }

  private[utils] def nextOnBoolean(condition: Boolean, fromPage: Identifier, onSuccessPage: Identifier, onFailPage: Identifier): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswerBoolean(fromPage) match {
        case Some(`condition`) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def nextOnString(condition: String, fromPage: Identifier, onSuccessPage: Identifier, onFailPage: Identifier): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswerString(fromPage) match {
        case Some(`condition`) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def toNextPage(fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) = fromPage -> {
    _ => pageIdToPageLoad(toPage)
  }

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
    nextOnFalse(ThresholdNextThirtyDaysId, ThresholdPreviousThirtyDaysId),
    nextOnFalse(ThresholdPreviousThirtyDaysId, ThresholdInTwelveMonthsId),
    nextOnFalse(ThresholdInTwelveMonthsId, VoluntaryRegistrationId),
    nextOnBoolean(true, VoluntaryRegistrationId, TurnoverEstimateId, ChoseNotToRegisterId),
    toNextPage(TurnoverEstimateId, CompletionCapacityId),
    nextOnString("noneOfThese", CompletionCapacityId, CompletionCapacityFillingInForId, InvolvedInOtherBusinessId),
    toNextPage(CompletionCapacityFillingInForId, InvolvedInOtherBusinessId),
    nextOnBoolean(false, InvolvedInOtherBusinessId, InternationalActivitiesId, EligibilityDropoutId),
    nextOnBoolean(false, InternationalActivitiesId, AnnualAccountingSchemeId, EligibilityDropoutId),
    nextOnBoolean(false, AnnualAccountingSchemeId, ZeroRatedSalesId, EligibilityDropoutId),
    nextOnBoolean(false, ZeroRatedSalesId, VATRegistrationExceptionId, VATExemptionId),
    nextOnBoolean(false, VATExemptionId, VATRegistrationExceptionId, ApplyInWritingId),
    nextOnBoolean(false, VATRegistrationExceptionId, AgriculturalFlatRateSchemeId, EligibilityDropoutId),
    nextOnBoolean(false, AgriculturalFlatRateSchemeId, RacehorsesId, EligibilityDropoutId),
    nextOnBoolean(false, RacehorsesId, ApplicationUKNinoId, EligibilityDropoutId),
    nextOnBoolean(false, ApplicationUKNinoId, ApplicationUKNinoId, EligibilityDropoutId) //TODO edit when other pages have been added
  )

  def nextPage(id: Identifier, mode: Mode): UserAnswers => Call =
    routeMap.getOrElse(id, _ => routes.ThresholdNextThirtyDaysController.onPageLoad())
}
