/*
 * Copyright 2023 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.routes
import featureswitch.core.config._
import featureswitch.core.models.FeatureSwitch
import identifiers._
import models._
import play.api.Logging
import play.api.libs.json.Reads
import play.api.mvc.Call
import utils.DefaultImplicitJsonReads.BooleanReads

import javax.inject.{Inject, Singleton}

//scalastyle:off
@Singleton
class Navigator @Inject extends Logging with FeatureSwitching {

  def pageIdToPageLoad(pageId: Identifier): Call = pageId match {
    case FixedEstablishmentId => routes.FixedEstablishmentController.onPageLoad
    case BusinessEntityId => routes.BusinessEntityController.onPageLoad
    case BusinessEntityPartnershipId => routes.BusinessEntityPartnershipController.onPageLoad
    case BusinessEntityOverseasId => routes.BusinessEntityOverseasController.onPageLoad
    case ThresholdNextThirtyDaysId => routes.ThresholdNextThirtyDaysController.onPageLoad
    case ThresholdPreviousThirtyDaysId => routes.ThresholdPreviousThirtyDaysController.onPageLoad
    case VoluntaryRegistrationId => routes.VoluntaryRegistrationController.onPageLoad
    case ChoseNotToRegisterId => routes.ChoseNotToRegisterController.onPageLoad
    case ThresholdInTwelveMonthsId => routes.ThresholdInTwelveMonthsController.onPageLoad
    case InternationalActivitiesId => routes.InternationalActivitiesController.onPageLoad
    case RegisteringBusinessId => routes.RegisteringBusinessController.onPageLoad
    case VATRegistrationExceptionId => routes.VATRegistrationExceptionController.onPageLoad
    case EligibilityDropoutId(mode) => mode match {
      case InternationalActivitiesId.toString => routes.EligibilityDropoutController.internationalActivitiesDropout
      case mode => routes.EligibilityDropoutController.onPageLoad(mode)
    }
    case AgriculturalFlatRateSchemeId => routes.AgriculturalFlatRateSchemeController.onPageLoad
    case MtdInformationId => routes.MtdInformationController.onPageLoad
    case TaxableSuppliesInUkId => routes.TaxableSuppliesInUkController.onPageLoad
    case DateOfBusinessTransferId => routes.DateOfBusinessTransferController.onPageLoad
    case ThresholdTaxableSuppliesId => routes.ThresholdTaxableSuppliesController.onPageLoad
    case DoNotNeedToRegisterId => routes.DoNotNeedToRegisterController.onPageLoad
    case RegistrationReasonId => routes.RegistrationReasonController.onPageLoad
    case RegReasonResolverId => routes.RegReasonResolverController.resolve
    case PreviousBusinessNameId => routes.PreviousBusinessNameController.onPageLoad
    case VATNumberId => routes.VATNumberController.onPageLoad
    case KeepOldVrnId => routes.KeepOldVrnController.onPageLoad
    case TermsAndConditionsId => routes.TermsAndConditionsController.onPageLoad
    case page => logger.info(s"${page.toString} does not exist navigating to start of the journey")
      controllers.routes.FixedEstablishmentController.onPageLoad
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

  private[utils] def nextOnWithFeatureSwitch[T](condition: T,
                                                featureSwitch: FeatureSwitch,
                                                fromPage: Identifier, onSuccessPage: Identifier,
                                                featureSwitchSuccessPage: Identifier,
                                                onFailPage: Identifier
                                               )(implicit reads: Reads[T], appConfig: FrontendAppConfig): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswer[T](fromPage) match {
        case Some(`condition`) if isEnabled(featureSwitch) => pageIdToPageLoad(featureSwitchSuccessPage)
        case Some(`condition`) => pageIdToPageLoad(onSuccessPage)
        case _ => pageIdToPageLoad(onFailPage)
      }
    }
  }

  private[utils] def toNextPage(fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) =
    fromPage -> { _ => pageIdToPageLoad(toPage) }

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
    BusinessEntityId -> { userAnswers =>
      userAnswers.getAnswer[BusinessEntity](BusinessEntityId) match {
        case Some(Other) => routes.BusinessEntityOtherController.onPageLoad
        case Some(Partnership) => routes.BusinessEntityPartnershipController.onPageLoad
        case Some(_) => routes.AgriculturalFlatRateSchemeController.onPageLoad
        case _ => routes.BusinessEntityController.onPageLoad
      }
    },
    BusinessEntityPartnershipId -> { userAnswers =>
      userAnswers.getAnswer[BusinessEntity](BusinessEntityId) match {
        case Some(_: PartnershipType) => routes.AgriculturalFlatRateSchemeController.onPageLoad
        case _ => routes.BusinessEntityController.onPageLoad
      }
    },
    BusinessEntityOtherId -> { userAnswers =>
      userAnswers.getAnswer[BusinessEntity](BusinessEntityId) match {
        case Some(Division) => routes.EligibilityDropoutController.onPageLoad(BusinessEntityId.toString)
        case Some(_: OtherType) => routes.AgriculturalFlatRateSchemeController.onPageLoad
        case _ => routes.BusinessEntityController.onPageLoad
      }
    },
    nextOn(false,
      fromPage = AgriculturalFlatRateSchemeId,
      onSuccessPage = InternationalActivitiesId,
      onFailPage = EligibilityDropoutId(AgriculturalFlatRateSchemeId.toString)
    ),
    FixedEstablishmentId -> { userAnswers =>
      userAnswers.fixedEstablishment match {
        case Some(true) => pageIdToPageLoad(BusinessEntityId)
        case Some(false) => pageIdToPageLoad(BusinessEntityOverseasId)
        case _ => pageIdToPageLoad(EligibilityDropoutId(InternationalActivitiesId.toString))
      }
    },
    BusinessEntityOverseasId -> { userAnswers =>
      userAnswers.getAnswer[BusinessEntity](BusinessEntityId) match {
        case Some(NETP) => pageIdToPageLoad(AgriculturalFlatRateSchemeId)
        case Some(Overseas) => pageIdToPageLoad(AgriculturalFlatRateSchemeId)
        case _ => pageIdToPageLoad(FixedEstablishmentId)
      }
    },
    nextOn(false,
      fromPage = InternationalActivitiesId,
      onSuccessPage = RegisteringBusinessId,
      onFailPage = EligibilityDropoutId(InternationalActivitiesId.toString)
    ),
    toNextPage(
      fromPage = RegisteringBusinessId,
      toPage = RegistrationReasonId
    ),
    RegistrationReasonId -> { userAnswers =>
      userAnswers.fixedEstablishment match {
        case Some(false) if userAnswers.registrationReason.exists(answer => List(TakingOverBusiness, ChangingLegalEntityOfBusiness).contains(answer)) =>
          pageIdToPageLoad(RegReasonResolverId)
        case Some(false) =>
          pageIdToPageLoad(TaxableSuppliesInUkId)
        case Some(true) =>
          pageIdToPageLoad(RegReasonResolverId)
      }
    },
    RegReasonResolverId -> { userAnswers =>
      userAnswers.fixedEstablishment match {
        case Some(_) if Seq(TakingOverBusiness, ChangingLegalEntityOfBusiness).exists(userAnswers.registrationReason.contains(_)) =>
          pageIdToPageLoad(DateOfBusinessTransferId)
        case Some(true) if Seq(UkEstablishedOverseasExporter, SettingUpVatGroup).exists(userAnswers.registrationReason.contains(_)) =>
          pageIdToPageLoad(MtdInformationId)
        case Some(true) =>
          pageIdToPageLoad(ThresholdInTwelveMonthsId)
        case Some(false) =>
          pageIdToPageLoad(ThresholdTaxableSuppliesId)
      }
    },
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
    nextOn(true,
      fromPage = VATRegistrationExceptionId,
      onSuccessPage = MtdInformationId,
      onFailPage = MtdInformationId
    ),
    nextOn(true,
      fromPage = VoluntaryRegistrationId,
      onSuccessPage = MtdInformationId,
      onFailPage = ChoseNotToRegisterId
    ),
    TaxableSuppliesInUkId -> { userAnswers =>
      if (userAnswers.taxableSuppliesInUk.contains(true)) {
        pageIdToPageLoad(RegReasonResolverId)
      } else {
        pageIdToPageLoad(DoNotNeedToRegisterId)
      }
    },
    toNextPage(
      fromPage = ThresholdTaxableSuppliesId,
      toPage = MtdInformationId
    ),
    toNextPage(
      fromPage = DateOfBusinessTransferId,
      toPage = PreviousBusinessNameId
    ),
    toNextPage(
      fromPage = PreviousBusinessNameId,
      toPage = VATNumberId
    ),
    toNextPage(
      fromPage = VATNumberId,
      toPage = KeepOldVrnId
    ),
    nextOn(true,
      fromPage = KeepOldVrnId,
      onSuccessPage = TermsAndConditionsId,
      onFailPage = MtdInformationId
    ),
    toNextPage(
      fromPage = TermsAndConditionsId,
      toPage = MtdInformationId
    )
  )

  def nextPage(id: Identifier, mode: Mode): UserAnswers => Call =
    routeMap.getOrElse(id, _ => controllers.routes.FixedEstablishmentController.onPageLoad)
}
