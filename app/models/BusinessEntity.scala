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

package models

import play.api.libs.json._

sealed trait BusinessEntity

case object UKCompany extends BusinessEntity
case object SoleTrader extends BusinessEntity
case object Partnership extends BusinessEntity
case object Other extends BusinessEntity

sealed trait PartnershipType extends BusinessEntity
case object GeneralPartnership extends PartnershipType
case object LimitedPartnership extends PartnershipType
case object ScottishPartnership extends PartnershipType
case object ScottishLimitedPartnership extends PartnershipType
case object LimitedLiabilityPartnership extends PartnershipType

sealed trait OtherType extends BusinessEntity
case object CharitableIncorporatedOrganisation extends OtherType
case object NonIncorporatedTrust extends OtherType
case object RegisteredSociety extends OtherType
case object UnincorporatedAssociation extends OtherType
case object Division extends OtherType
case object VatGroup extends OtherType

sealed trait OverseasType extends BusinessEntity
case object Overseas extends OverseasType
case object NETP extends OverseasType

object BusinessEntity {
  val ukCompanyKey = "50"
  val soleTraderKey = "Z1"
  val partnershipKey = "partnership"
  val generalPartnershipKey = "61"
  val limitedPartnershipKey = "62"
  val scottishPartnershipKey = "58"
  val scottishLimitedPartnershipKey = "59"
  val limitedLiabilityPartnershipKey = "52"
  val otherKey = "other"
  val charitableIncorporatedOrganisationKey = "53"
  val nonIncorporatedTrustKey = "60"
  val registeredSocietyKey = "54"
  val unincorporatedAssociationKey = "63"
  val divisionKey = "65"
  val netpKey = "NETP"
  val overseasKey = "55"

  // scalastyle:off
  implicit def writes[T <: BusinessEntity]: Writes[T] = Writes[T] {
    case UKCompany => JsString(ukCompanyKey)
    case SoleTrader => JsString(soleTraderKey)
    case Partnership => JsString(partnershipKey)
    case GeneralPartnership => JsString(generalPartnershipKey)
    case LimitedPartnership => JsString(limitedPartnershipKey)
    case ScottishPartnership => JsString(scottishPartnershipKey)
    case ScottishLimitedPartnership => JsString(scottishLimitedPartnershipKey)
    case LimitedLiabilityPartnership => JsString(limitedLiabilityPartnershipKey)
    case Other => JsString(otherKey)
    case CharitableIncorporatedOrganisation => JsString(charitableIncorporatedOrganisationKey)
    case NonIncorporatedTrust => JsString(nonIncorporatedTrustKey)
    case RegisteredSociety => JsString(registeredSocietyKey)
    case UnincorporatedAssociation => JsString(unincorporatedAssociationKey)
    case Division => JsString(divisionKey)
    case NETP => JsString(netpKey)
    case Overseas => JsString(overseasKey)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Business Entity: $unknownKey")
  }

  def businessEntityToString(businessEntity: BusinessEntity): String = businessEntity match {
    case UKCompany => "businessEntity.limited-company"
    case SoleTrader => "businessEntity.soletrader"
    case Partnership => "businessEntity.partnership"
    case GeneralPartnership => "businessEntity.general-partnership"
    case LimitedPartnership => "businessEntity.limited-partnership"
    case ScottishPartnership => "businessEntity.scottish-partnership"
    case ScottishLimitedPartnership => "businessEntity.scottish-limited-partnership"
    case LimitedLiabilityPartnership => "businessEntity.limited-liability-partnership"
    case Other => "businessEntity.other"
    case CharitableIncorporatedOrganisation => "businessEntity.charitable-incorporated-organisation"
    case NonIncorporatedTrust => "businessEntity.non-incorporated-trust"
    case RegisteredSociety => "businessEntity.registered-society"
    case UnincorporatedAssociation => "businessEntity.unincorporated-association"
    case Division => "businessEntity.division"
    case NETP => "businessEntityOverseas.netp"
    case Overseas => "businessEntityOverseas.overseas"
  }

  implicit val jsonReads: Reads[BusinessEntity] = Reads[BusinessEntity] {
    case JsString(`ukCompanyKey`) => JsSuccess(UKCompany)
    case JsString(`soleTraderKey`) => JsSuccess(SoleTrader)
    case JsString(`partnershipKey`) => JsSuccess(Partnership)
    case JsString(`generalPartnershipKey`) => JsSuccess(GeneralPartnership)
    case JsString(`limitedPartnershipKey`) => JsSuccess(LimitedPartnership)
    case JsString(`scottishPartnershipKey`) => JsSuccess(ScottishPartnership)
    case JsString(`scottishLimitedPartnershipKey`) => JsSuccess(ScottishLimitedPartnership)
    case JsString(`limitedLiabilityPartnershipKey`) => JsSuccess(LimitedLiabilityPartnership)
    case JsString(`otherKey`) => JsSuccess(Other)
    case JsString(`charitableIncorporatedOrganisationKey`) => JsSuccess(CharitableIncorporatedOrganisation)
    case JsString(`nonIncorporatedTrustKey`) => JsSuccess(NonIncorporatedTrust)
    case JsString(`registeredSocietyKey`) => JsSuccess(RegisteredSociety)
    case JsString(`unincorporatedAssociationKey`) => JsSuccess(UnincorporatedAssociation)
    case JsString(`divisionKey`) => JsSuccess(Division)
    case JsString(`netpKey`) => JsSuccess(NETP)
    case JsString(`overseasKey`) => JsSuccess(Overseas)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Business Entity: $unknownKey")
  }

  implicit val jsonFormat: Format[BusinessEntity] = Format[BusinessEntity](jsonReads, writes)
}
