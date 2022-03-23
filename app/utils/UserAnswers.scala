/*
 * Copyright 2022 HM Revenue & Customs
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

import identifiers._
import models._
import models.requests.DataRequest
import play.api.libs.json.{JsValue, Reads}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.cache.client.CacheMap

import javax.inject.Singleton

// scalastyle:off
@Singleton
class UserAnswers(val cacheMap: CacheMap) extends Enumerable.Implicits {
  def fixedEstablishment: Option[Boolean] = cacheMap.getEntry[Boolean](FixedEstablishmentId.toString)

  def businessEntity: Option[BusinessEntity] = cacheMap.getEntry[BusinessEntity](BusinessEntityId.toString)

  def isPartnership: Boolean = businessEntity match {
    case Some(_: PartnershipType) => true
    case _ => false
  }

  def isUkCompany: Boolean = businessEntity match {
    case Some(UKCompany) => true
    case _ => false
  }

  def isOverseas: Boolean = businessEntity match {
    case Some(_: OverseasType) => true
    case _ => false
  }

  def togcColeKey: String = registrationReason match {
    case Some(ChangingLegalEntityOfBusiness) => "cole"
    case _ => "togc"
  }

  def racehorses: Option[Boolean] = cacheMap.getEntry[Boolean](RacehorsesId.toString)

  def agriculturalFlatRateScheme: Option[Boolean] = cacheMap.getEntry[Boolean](AgriculturalFlatRateSchemeId.toString)

  def vatRegistrationException: Option[Boolean] = cacheMap.getEntry[Boolean](VATRegistrationExceptionId.toString)

  def vatExceptionKickout: Option[Boolean] = cacheMap.getEntry[Boolean](VATExceptionKickoutId.toString)

  def vatExemption: Option[Boolean] = cacheMap.getEntry[Boolean](VATExemptionId.toString)

  def zeroRatedSales: Option[Boolean] = cacheMap.getEntry[Boolean](ZeroRatedSalesId.toString)

  def internationalActivities: Option[Boolean] = cacheMap.getEntry[Boolean](InternationalActivitiesId.toString)

  def involvedInOtherBusiness: Option[Boolean] = cacheMap.getEntry[Boolean](InvolvedInOtherBusinessId.toString)

  def turnoverEstimate: Option[TurnoverEstimateFormElement] = cacheMap.getEntry[TurnoverEstimateFormElement](TurnoverEstimateId.toString)

  def thresholdInTwelveMonths: Option[ConditionalDateFormElement] = cacheMap.getEntry[ConditionalDateFormElement](ThresholdInTwelveMonthsId.toString)

  def thresholdTaxableSupplies: Option[DateFormElement] = cacheMap.getEntry[DateFormElement](ThresholdTaxableSuppliesId.toString)

  def voluntaryRegistration: Option[Boolean] = cacheMap.getEntry[Boolean](VoluntaryRegistrationId.toString)

  def thresholdPreviousThirtyDays: Option[ConditionalDateFormElement] = cacheMap.getEntry[ConditionalDateFormElement](ThresholdPreviousThirtyDaysId.toString)

  def thresholdNextThirtyDays: Option[ConditionalDateFormElement] = cacheMap.getEntry[ConditionalDateFormElement](ThresholdNextThirtyDaysId.toString)

  def registeringBusiness: Option[RegisteringBusiness] = cacheMap.getEntry[RegisteringBusiness](RegisteringBusinessId.toString)

  def nino: Option[Boolean] = cacheMap.getEntry[Boolean](NinoId.toString)

  def taxableSuppliesInUk: Option[Boolean] = cacheMap.getEntry[Boolean](TaxableSuppliesInUkId.toString)

  def getAnswer[T](id: Identifier)(implicit reads: Reads[T]): Option[T] = cacheMap.getEntry[T](id.toString)

  def getAnswerFromIdString[T](id: String)(implicit reads: Reads[T]): Option[JsValue] = cacheMap.getEntry[JsValue](id)

  def getAnswerBoolean(id: Identifier): Option[Boolean] = cacheMap.getEntry[Boolean](id.toString)

  def registrationReason: Option[RegistrationReason] = cacheMap.getEntry[RegistrationReason](RegistrationReasonId.toString)

  def dateOfBusinessTransfer: Option[DateFormElement] = cacheMap.getEntry[DateFormElement](DateOfBusinessTransferId.toString)

  def previousBusinessName: Option[String] = cacheMap.getEntry[String](PreviousBusinessNameId.toString)

  def vatNumber: Option[String] = cacheMap.getEntry[String](VATNumberId.toString)

  def keepOldVrn: Option[Boolean] = cacheMap.getEntry[Boolean](KeepOldVrnId.toString)

  def termsAndConditions: Option[Boolean] = cacheMap.getEntry[Boolean](TermsAndConditionsId.toString)
}