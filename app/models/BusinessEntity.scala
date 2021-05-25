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
case class Partnership() extends BusinessEntity
object GeneralPartnership extends Partnership
object LimitedPartnership extends Partnership
object ScottishPartnership extends Partnership
object ScottishLimitedPartnership extends Partnership
object LimitedLiabilityPartnership extends Partnership
case class Other() extends BusinessEntity
object CharitableIncorporatedOrganisation extends Other
object NonIncorporatedTrust extends Other
object RegisteredSociety extends Other
object UnincorporatedAssociation extends Other
object GovernmentOrganisationOrPublicBody extends Other
object Division extends BusinessEntity //TODO Change to extends Other for SAR-7632
object VatGroup extends Other

object BusinessEntity {
  val ukCompanyKey = "uk-company"
  val soleTraderKey = "sole-trader"
  val partnershipKey = "partnership"
  val generalPartnershipKey = "general-partnership"
  val limitedPartnershipKey = "limited-partnership"
  val scottishPartnershipKey = "scottish-partnership"
  val scottishLimitedPartnershipKey = "scottish-limited-partnership"
  val limitedLiabilityPartnershipKey = "limited-liability-partnership"
  val otherKey = "other"
  val charitableIncorporatedOrganisationKey = "charitable-incorporated-organisation"
  val nonIncorporatedTrustKey = "non-incorporated-trust"
  val registeredSocietyKey = "registered-society"
  val unincorporatedAssociationKey = "unincorporated-association"
  val governmentOrganisationOrPublicBodyKey = "government-organisation-or-public-body"
  val divisionKey = "division"
  val vatGroupKey = "vat-group"

  def businessEntityToString(businessEntity: BusinessEntity)(implicit messages: Messages): String = businessEntity match {
    case UKCompany => messages("businessEntity.ukcompany")
    case SoleTrader => messages("businessEntity.soletrader")
    case Partnership() => messages("businessEntity.partnership")
    case GeneralPartnership => messages("businessEntity.general-partnership")
    case LimitedPartnership => messages("businessEntity.limited-partnership")
    case ScottishPartnership => messages("businessEntity.scottish-partnership")
    case ScottishLimitedPartnership => messages("businessEntity.scottish-limited-partnership")
    case LimitedLiabilityPartnership => messages("businessEntity.limited-liability-partnership")
    case Other() => messages("businessEntity.other")
    case CharitableIncorporatedOrganisation => messages("businessEntity.charitable-incorporated-organisation")
    case NonIncorporatedTrust => messages("businessEntity.non-incorporated-trust")
    case RegisteredSociety => messages("businessEntity.registered-society")
    case UnincorporatedAssociation => messages("businessEntity.unincorporated-association")
    case GovernmentOrganisationOrPublicBody => messages("businessEntity.government-organisation-or-public-body")
    case Division => messages("businessEntity.division")
    case VatGroup => messages("businessEntity.vat-group")
  }

  implicit val jsonReads: Reads[BusinessEntity] = Reads[BusinessEntity] {
    case JsString(`ukCompanyKey`) => JsSuccess(UKCompany)
    case JsString(`soleTraderKey`) => JsSuccess(SoleTrader)
    case JsString(`partnershipKey`) => JsSuccess(Partnership())
    case JsString(`generalPartnershipKey`) => JsSuccess(GeneralPartnership)
    case JsString(`limitedPartnershipKey`) => JsSuccess(LimitedPartnership)
    case JsString(`scottishPartnershipKey`) => JsSuccess(ScottishPartnership)
    case JsString(`scottishLimitedPartnershipKey`) => JsSuccess(ScottishLimitedPartnership)
    case JsString(`limitedLiabilityPartnershipKey`) => JsSuccess(LimitedLiabilityPartnership)
    case JsString(`otherKey`) => JsSuccess(Other())
    case JsString(`charitableIncorporatedOrganisationKey`) => JsSuccess(CharitableIncorporatedOrganisation)
    case JsString(`nonIncorporatedTrustKey`) => JsSuccess(NonIncorporatedTrust)
    case JsString(`registeredSocietyKey`) => JsSuccess(RegisteredSociety)
    case JsString(`unincorporatedAssociationKey`) => JsSuccess(UnincorporatedAssociation)
    case JsString(`governmentOrganisationOrPublicBodyKey`) => JsSuccess(GovernmentOrganisationOrPublicBody)
    case JsString(`divisionKey`) => JsSuccess(Division)
    case JsString(`vatGroupKey`) => JsSuccess(VatGroup)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Business Entity: $unknownKey")
  }

  implicit val jsonWrites: Writes[BusinessEntity] = Writes[BusinessEntity] {
    case UKCompany => JsString(ukCompanyKey)
    case SoleTrader => JsString(soleTraderKey)
    case Partnership() => JsString(partnershipKey)
    case GeneralPartnership => JsString(generalPartnershipKey)
    case LimitedPartnership => JsString(limitedPartnershipKey)
    case ScottishPartnership => JsString(scottishPartnershipKey)
    case ScottishLimitedPartnership => JsString(scottishLimitedPartnershipKey)
    case LimitedLiabilityPartnership => JsString(limitedLiabilityPartnershipKey)
    case Other() => JsString(otherKey)
    case CharitableIncorporatedOrganisation => JsString(charitableIncorporatedOrganisationKey)
    case NonIncorporatedTrust => JsString(nonIncorporatedTrustKey)
    case RegisteredSociety => JsString(registeredSocietyKey)
    case UnincorporatedAssociation => JsString(unincorporatedAssociationKey)
    case GovernmentOrganisationOrPublicBody => JsString(governmentOrganisationOrPublicBodyKey)
    case Division => JsString(divisionKey)
    case VatGroup => JsString(vatGroupKey)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Business Entity: $unknownKey")
  }

  implicit val jsonFormat: Format[BusinessEntity] = Format[BusinessEntity](jsonReads, jsonWrites)
}
