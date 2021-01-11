/*
 * Copyright 2021 HM Revenue & Customs
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
import models._
import play.api.Logger
import play.api.libs.json.Reads
import play.api.mvc.Call
import utils.DefaultImplicitJsonReads.BooleanReads

//scalastyle:off
@Singleton
class Navigator @Inject()() {

  def pageIdToPageLoad(pageId: Identifier): Call = pageId match {
    case BusinessEntityId => routes.BusinessEntityController.onPageLoad()
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
    case VATExceptionKickoutId => routes.VATExceptionKickoutController.onPageLoad()
    case VATRegistrationExceptionId => routes.VATRegistrationExceptionController.onPageLoad()
    case ApplyInWritingId => routes.ApplyInWritingController.onPageLoad()
    case EligibilityDropoutId(mode) => mode match {
      case InternationalActivitiesId.toString => routes.EligibilityDropoutController.internationalActivitiesDropout()
      case mode => routes.EligibilityDropoutController.onPageLoad(mode)
    }
    case AgriculturalFlatRateSchemeId => routes.AgriculturalFlatRateSchemeController.onPageLoad()
    case RacehorsesId => routes.RacehorsesController.onPageLoad()
    case VoluntaryInformationId => routes.VoluntaryInformationController.onPageLoad()
    case MandatoryInformationId => routes.MandatoryInformationController.onPageLoad()
    case EligibleId => routes.EligibleController.onPageLoad()
    case page => {
      Logger.info(s"${page.toString} does not exist navigating to start of the journey")
      routes.IntroductionController.onPageLoad()
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

  private[utils] def nextOn12MonthThresholdConditionalFormElement(condition: Boolean, fromPage: Identifier, onSuccessPage: Identifier, onFailPage: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.thresholdInTwelveMonths match {
        case Some(ConditionalDateFormElement(`condition`, _)) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def nextOnNextThirtyDaysThresholdConditionalFormElement(condition: Boolean, fromPage: Identifier, onSuccessPage: Identifier, onFailPage: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.thresholdNextThirtyDays match {
        case Some(ConditionalDateFormElement(`condition`, _)) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def checkZeroRatedSalesVoluntaryQuestion(fromPage: Identifier, mandatoryTrue: Identifier, mandatoryFalse: Identifier, onFailPage: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> { userAns =>
      if (userAns.zeroRatedSales.contains(false)) {
        if (ThresholdHelper.q1DefinedAndTrue(userAns) || ThresholdHelper.nextThirtyDaysDefinedAndTrue(userAns)) {
          pageIdToPageLoad(mandatoryTrue)
        } else {
          pageIdToPageLoad(mandatoryFalse)
        }
      }
     else if (userAns.zeroRatedSales.contains(true) && userAns.vatRegistrationException.contains(true)) {
        pageIdToPageLoad(mandatoryTrue)
      }
      else if (userAns.zeroRatedSales.contains(true) && ThresholdHelper.nextThirtyDaysDefinedAndFalse(userAns)) {
        pageIdToPageLoad(mandatoryFalse)
      }
      else {
        pageIdToPageLoad(onFailPage)
      }
    }
  }

  private def checkVoluntaryQuestion(fromPage: Identifier, mandatoryTrue: Identifier, mandatoryFalse: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> { userAns =>
      if (ThresholdHelper.q1DefinedAndTrue(userAns) || ThresholdHelper.nextThirtyDaysDefinedAndTrue(userAns)) {
        pageIdToPageLoad(mandatoryTrue)
      } else {
        pageIdToPageLoad(mandatoryFalse)
      }
    }
  }

  private def checkBusinessEntity(fromPage: Identifier, ukCompanyTrue: Identifier, ukCompanyFalse: Identifier, onFailPage: Identifier):
  (Identifier, UserAnswers => Call) = {
    fromPage -> { userAns =>
      if (userAns.businessEntity.contains(UKCompany)) {
        if (userAns.internationalActivities.contains(false)) {
          pageIdToPageLoad(ukCompanyTrue)
        } else {
          pageIdToPageLoad(onFailPage)
        }
      }
      else if (userAns.internationalActivities.contains(false)) {
        pageIdToPageLoad(ukCompanyFalse)
      }
      else {
        pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def toNextPage(fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) =
    fromPage -> { _ => pageIdToPageLoad(toPage) }

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
    BusinessEntityId -> { userAnswers =>
      userAnswers.getAnswer[BusinessEntity](BusinessEntityId) match {
        case Some(Division) => routes.EligibilityDropoutController.onPageLoad(BusinessEntityId.toString)
        case Some(_) => routes.AgriculturalFlatRateSchemeController.onPageLoad()
        case _ => routes.BusinessEntityController.onPageLoad()
      }
    },
    nextOn(false,
      fromPage = AgriculturalFlatRateSchemeId,
      onSuccessPage = InternationalActivitiesId,
      onFailPage = EligibilityDropoutId(AgriculturalFlatRateSchemeId.toString)
    ),
    checkBusinessEntity(
      fromPage = InternationalActivitiesId,
      ukCompanyTrue = InvolvedInOtherBusinessId,
      ukCompanyFalse = VATExceptionKickoutId,
      onFailPage = EligibilityDropoutId(InternationalActivitiesId.toString)
    ),
    nextOn(true,
      fromPage = VATExceptionKickoutId,
      onSuccessPage = EligibilityDropoutId(VATExceptionKickoutId.toString),
      onFailPage = EligibilityDropoutId(VATRegistrationExceptionId.toString)
    ),
    nextOn(false,
      fromPage = InvolvedInOtherBusinessId,
      onSuccessPage = RacehorsesId,
      onFailPage = VATExceptionKickoutId
    ),
    nextOn(false,
      fromPage = RacehorsesId,
      onSuccessPage = AnnualAccountingSchemeId,
      onFailPage = VATExceptionKickoutId
    ),
    nextOn(false,
      fromPage = AnnualAccountingSchemeId,
      onSuccessPage = RegisteringBusinessId,
      onFailPage = VATExceptionKickoutId
    ),
    nextOn(true,
      fromPage = RegisteringBusinessId,
      onSuccessPage = NinoId,
      onFailPage = VATExceptionKickoutId
    ),
    nextOn(true,
      fromPage = NinoId,
      onSuccessPage = ThresholdInTwelveMonthsId,
      onFailPage = VATExceptionKickoutId
    ),
    nextOn12MonthThresholdConditionalFormElement(true,
      fromPage = ThresholdInTwelveMonthsId,
      onSuccessPage = ThresholdPreviousThirtyDaysId,
      onFailPage = ThresholdNextThirtyDaysId
    ),
    toNextPage(
      fromPage = ThresholdPreviousThirtyDaysId,
      toPage = VATRegistrationExceptionId
    ),
    nextOnNextThirtyDaysThresholdConditionalFormElement(true,
      fromPage = ThresholdNextThirtyDaysId,
      onSuccessPage = VATRegistrationExceptionId,
      onFailPage = VoluntaryRegistrationId
    ),
    toNextPage(
      fromPage = VATRegistrationExceptionId,
      toPage = TurnoverEstimateId
    ),
    toNextPage(
      fromPage = TurnoverEstimateId,
      toPage = ZeroRatedSalesId
    ),
    checkZeroRatedSalesVoluntaryQuestion(
      ZeroRatedSalesId,
      MandatoryInformationId,
      VoluntaryInformationId,
      VATExemptionId
    ),
    checkVoluntaryQuestion(
      fromPage = VATExemptionId,
      mandatoryTrue = MandatoryInformationId,
      mandatoryFalse = VoluntaryInformationId
    ),
    nextOn(true,
      fromPage = VoluntaryRegistrationId,
      onSuccessPage = TurnoverEstimateId,
      onFailPage = ChoseNotToRegisterId
    ),
    toNextPage(VoluntaryInformationId, EligibleId)
  )

  def nextPage(id: Identifier, mode: Mode): UserAnswers => Call =
    routeMap.getOrElse(id, _ => routes.IntroductionController.onPageLoad())
}
