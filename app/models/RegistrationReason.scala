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

sealed trait RegistrationReason

case object SellingGoodsAndServices extends RegistrationReason

case object TakingOverBusiness extends RegistrationReason

case object ChangingLegalEntityOfBusiness extends RegistrationReason

case object SettingUpVatGroup extends RegistrationReason

case object UkEstablishedOverseasExporter extends RegistrationReason

object RegistrationReason {
  val sellingGoodsAndServicesKey = "selling-goods-and-services"
  val takingOverBusinessKey = "taking-over-business"
  val changingLegalEntityOfBusinessKey = "changing-legal-entity"
  val settingUpVatGroupKey = "setting-up-vat-group"
  val ukEstablishedOverseasExporterKey = "overseas-exporter"

  implicit def writes[T <: RegistrationReason]: Writes[T] = Writes[T] {
    case SellingGoodsAndServices => JsString(sellingGoodsAndServicesKey)
    case TakingOverBusiness => JsString(takingOverBusinessKey)
    case ChangingLegalEntityOfBusiness => JsString(changingLegalEntityOfBusinessKey)
    case SettingUpVatGroup => JsString(settingUpVatGroupKey)
    case UkEstablishedOverseasExporter => JsString(ukEstablishedOverseasExporterKey)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Registration Reason: $unknownKey")
  }
  def registrationReasonToString(registrationReason: RegistrationReason): String = registrationReason match {
    case SellingGoodsAndServices => "registrationReason.sellingGoods.radio"
    case TakingOverBusiness => "registrationReason.takingOver.radio"
    case ChangingLegalEntityOfBusiness => "registrationReason.changingEntity.radio"
    case SettingUpVatGroup => "registrationReason.settingUp.radio"
    case UkEstablishedOverseasExporter => "registrationReason.ukExporter.radio"
  }

  implicit val jsonReads: Reads[RegistrationReason] = Reads[RegistrationReason] {
    case JsString(`sellingGoodsAndServicesKey`) => JsSuccess(SellingGoodsAndServices)
    case JsString(`takingOverBusinessKey`) => JsSuccess(TakingOverBusiness)
    case JsString(`changingLegalEntityOfBusinessKey`) => JsSuccess(ChangingLegalEntityOfBusiness)
    case JsString(`settingUpVatGroupKey`) => JsSuccess(SettingUpVatGroup)
    case JsString(`ukEstablishedOverseasExporterKey`) => JsSuccess(UkEstablishedOverseasExporter)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Registration Reason: $unknownKey")
  }

  implicit val jsonFormat: Format[RegistrationReason] = Format[RegistrationReason](jsonReads, writes)
}




