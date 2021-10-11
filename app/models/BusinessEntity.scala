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

package models

import play.api.i18n.Messages
import play.api.libs.json._

sealed trait BusinessEntity

object UKCompany extends BusinessEntity
object SoleTrader extends BusinessEntity
object Partnership extends BusinessEntity
object Other extends BusinessEntity

sealed trait PartnershipType extends BusinessEntity
object GeneralPartnership extends PartnershipType
object LimitedPartnership extends PartnershipType
object ScottishPartnership extends PartnershipType
object ScottishLimitedPartnership extends PartnershipType
object LimitedLiabilityPartnership extends PartnershipType

sealed trait OtherType extends BusinessEntity
object CharitableIncorporatedOrganisation extends OtherType
object NonIncorporatedTrust extends OtherType
object RegisteredSociety extends OtherType
object UnincorporatedAssociation extends OtherType
object Division extends OtherType
object VatGroup extends OtherType

sealed trait OverseasType extends BusinessEntity
object Overseas extends OverseasType
object NETP extends OverseasType

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
  val vatGroupKey = "64"
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
    case VatGroup => JsString(vatGroupKey)
    case NETP => JsString(netpKey)
    case Overseas => JsString(overseasKey)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Business Entity: $unknownKey")
  }

  def businessEntityToString(businessEntity: BusinessEntity)(implicit messages: Messages): String = businessEntity match {
    case UKCompany => messages("businessEntity.ukcompany")
    case SoleTrader => messages("businessEntity.soletrader")
    case Partnership => messages("businessEntity.partnership")
    case GeneralPartnership => messages("businessEntity.general-partnership")
    case LimitedPartnership => messages("businessEntity.limited-partnership")
    case ScottishPartnership => messages("businessEntity.scottish-partnership")
    case ScottishLimitedPartnership => messages("businessEntity.scottish-limited-partnership")
    case LimitedLiabilityPartnership => messages("businessEntity.limited-liability-partnership")
    case Other => messages("businessEntity.other")
    case CharitableIncorporatedOrganisation => messages("businessEntity.charitable-incorporated-organisation")
    case NonIncorporatedTrust => messages("businessEntity.non-incorporated-trust")
    case RegisteredSociety => messages("businessEntity.registered-society")
    case UnincorporatedAssociation => messages("businessEntity.unincorporated-association")
    case Division => messages("businessEntity.division")
    case VatGroup => messages("businessEntity.vat-group")
    case NETP => messages("businessEntityOverseas.netp")
    case Overseas => messages("businessEntityOverseas.overseas")
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
    case JsString(`vatGroupKey`) => JsSuccess(VatGroup)
    case JsString(`netpKey`) => JsSuccess(NETP)
    case JsString(`overseasKey`) => JsSuccess(Overseas)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Business Entity: $unknownKey")
  }

  implicit val jsonFormat: Format[BusinessEntity] = Format[BusinessEntity](jsonReads, writes)
}
