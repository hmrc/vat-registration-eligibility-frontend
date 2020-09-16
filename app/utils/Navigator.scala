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

import controllers.routes
import identifiers.{Identifier, _}
import javax.inject.{Inject, Singleton}
import models.{ConditionalDateFormElement, Mode}
import play.api.Logger
import play.api.libs.json.Reads
import play.api.mvc.Call
import utils.DefaultImplicitJsonReads.BooleanReads

@Singleton
class Navigator @Inject()() {

  def pageIdToPageLoad(pageId: Identifier): Call = pageId match {
    case ThresholdNextThirtyDaysId => routes.ThresholdNextThirtyDaysController.onPageLoad()
    case ThresholdPreviousThirtyDaysId => routes.ThresholdPreviousThirtyDaysController.onPageLoad()
    case VoluntaryRegistrationId => routes.VoluntaryRegistrationController.onPageLoad()
    case ChoseNotToRegisterId => routes.ChoseNotToRegisterController.onPageLoad()
    case ThresholdInTwelveMonthsId => routes.ThresholdInTwelveMonthsController.onPageLoad()
    case TurnoverEstimateId => routes.TurnoverEstimateController.onPageLoad()
    case InvolvedInOtherBusinessId => routes.InvolvedInOtherBusinessController.onPageLoad()
    case InternationalActivitiesId => routes.InternationalActivitiesController.onPageLoad()
    case AnnualAccountingSchemeId => routes.AnnualAccountingSchemeController.onPageLoad()
    case ZeroRatedSalesId => routes.ZeroRatedSalesController.onPageLoad()
    case RegisteringBusinessId => routes.RegisteringBusinessController.onPageLoad()
    case NinoId => routes.NinoController.onPageLoad()
    case VATExemptionId => routes.VATExemptionController.onPageLoad()
    case VATRegistrationExceptionId => routes.VATRegistrationExceptionController.onPageLoad()
    case ApplyInWritingId => routes.ApplyInWritingController.onPageLoad()
    case EligibilityDropoutId(mode) => routes.EligibilityDropoutController.onPageLoad(mode)
    case AgriculturalFlatRateSchemeId => routes.AgriculturalFlatRateSchemeController.onPageLoad()
    case RacehorsesId => routes.RacehorsesController.onPageLoad()
    case page => {
      Logger.info(s"${page.toString} does not exist navigating to start of the journey")
      routes.ThresholdNextThirtyDaysController.onPageLoad()
    }
  }

  private[utils] def nextOn[T](condition: T, fromPage: Identifier, onSuccessPage: Identifier, onFailPage: Identifier)
                              (implicit reads: Reads[T]): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswer[T](fromPage) match {
        case Some(`condition`) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def nextOnConditionalFormElement(condition: Boolean, fromPage: Identifier, onSuccessPage: Identifier, onFailPage: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.thresholdInTwelveMonths match {
        case Some(ConditionalDateFormElement(`condition`, _)) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private def lastThresholdQuestion(fromPage: Identifier, twelveMonthsTrue: Identifier, twelveMonthsFalse: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> { userAns =>
      if (ThresholdHelper.q1DefinedAndTrue(userAns)) {
        pageIdToPageLoad(twelveMonthsTrue)
      } else {
        if (ThresholdHelper.taxableTurnoverCheck(userAns)) {
          pageIdToPageLoad(twelveMonthsFalse)
        } else {
          pageIdToPageLoad(VoluntaryRegistrationId)
        }
      }
    }
  }

  private[utils] def toNextPage(fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) = fromPage -> {
    _ => pageIdToPageLoad(toPage)
  }

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
    nextOnConditionalFormElement(false, ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdPreviousThirtyDaysId),
    ThresholdNextThirtyDaysId -> { _ => pageIdToPageLoad(ThresholdPreviousThirtyDaysId) },
    lastThresholdQuestion(ThresholdPreviousThirtyDaysId, VATRegistrationExceptionId, TurnoverEstimateId),
    nextOn(true, VoluntaryRegistrationId, TurnoverEstimateId, ChoseNotToRegisterId),
    toNextPage(TurnoverEstimateId, InvolvedInOtherBusinessId),
    nextOn(false, InvolvedInOtherBusinessId, InternationalActivitiesId, EligibilityDropoutId(InvolvedInOtherBusinessId.toString)),
    nextOn(false, InternationalActivitiesId, AnnualAccountingSchemeId, EligibilityDropoutId(InternationalActivitiesId.toString)),
    nextOn(false, AnnualAccountingSchemeId, ZeroRatedSalesId, VATRegistrationExceptionId),
    nextOn(true, ZeroRatedSalesId, VATExemptionId, RegisteringBusinessId),
    nextOn(true, RegisteringBusinessId, NinoId, EligibilityDropoutId(InvolvedInOtherBusinessId.toString)),
    nextOn(true, NinoId, AgriculturalFlatRateSchemeId, EligibilityDropoutId(NinoId.toString)),
    nextOn(false, VATExemptionId, AgriculturalFlatRateSchemeId, ApplyInWritingId),
    nextOn(false, VATRegistrationExceptionId, TurnoverEstimateId, EligibilityDropoutId(VATRegistrationExceptionId.toString)),
    nextOn(false, AgriculturalFlatRateSchemeId, RacehorsesId, EligibilityDropoutId(AgriculturalFlatRateSchemeId.toString)),
    toNextPage(RacehorsesId, EligibilityDropoutId(RacehorsesId.toString))
  )

  def nextPage(id: Identifier, mode: Mode): UserAnswers => Call =
    routeMap.getOrElse(id, _ => routes.ThresholdNextThirtyDaysController.onPageLoad())
}
