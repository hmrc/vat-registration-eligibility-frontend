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

package forms

import forms.mappings.Mappings
import models.RegistrationReason._
import models._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

import javax.inject.{Inject, Singleton}

@Singleton
class RegistrationReasonFormProvider @Inject() extends FormErrorHelper with Mappings {

  val registrationReason: String = "value"
  val registrationReasonError: String = "registrationReason.error"

  def apply(): Form[RegistrationReason] = Form(
    single(
      registrationReason -> of(formatter)
    )
  )

  def formatter: Formatter[RegistrationReason] = new Formatter[RegistrationReason] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RegistrationReason] = {
      data.get(key) match {
        case Some(`sellingGoodsAndServicesKey`) => Right(SellingGoodsAndServices)
        case Some(`takingOverBusinessKey`) => Right(TakingOverBusiness)
        case Some(`changingLegalEntityOfBusinessKey`) => Right(ChangingLegalEntityOfBusiness)
        case Some(`settingUpVatGroupKey`) => Right(SettingUpVatGroup)
        case Some(`ukEstablishedOverseasExporterKey`) => Right(UkEstablishedOverseasExporter)
        case _ => Left(Seq(FormError(key, registrationReasonError)))
      }
    }

    override def unbind(key: String, value: RegistrationReason): Map[String, String] = {
      val stringValue = value match {
        case SellingGoodsAndServices => sellingGoodsAndServicesKey
        case TakingOverBusiness => takingOverBusinessKey
        case ChangingLegalEntityOfBusiness => changingLegalEntityOfBusinessKey
        case SettingUpVatGroup => settingUpVatGroupKey
        case UkEstablishedOverseasExporter => ukEstablishedOverseasExporterKey
      }
      Map(key -> stringValue)
    }
  }

}
