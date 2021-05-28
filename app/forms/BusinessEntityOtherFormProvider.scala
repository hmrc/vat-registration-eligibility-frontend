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
import models.BusinessEntity._
import models._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

import javax.inject.Singleton

@Singleton
class BusinessEntityOtherFormProvider extends FormErrorHelper with Mappings {

  val businessEntity: String = "value"
  val businessEntityError: String = "businessEntityOther.error.required"

  def apply(): Form[OtherType] = Form(
    single(
      businessEntity -> of(formatter)
    )
  )

  def formatter: Formatter[OtherType] = new Formatter[OtherType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], OtherType] = {
      data.get(key) match {
        case Some(`charitableIncorporatedOrganisationKey`) => Right(CharitableIncorporatedOrganisation)
        case Some(`nonIncorporatedTrustKey`) => Right(NonIncorporatedTrust)
        case Some(`registeredSocietyKey`) => Right(RegisteredSociety)
        case Some(`unincorporatedAssociationKey`) => Right(UnincorporatedAssociation)
        case Some(`divisionKey`) => Right(Division)
        case Some(`vatGroupKey`) => Right(VatGroup)
        case _ => Left(Seq(FormError(key, businessEntityError)))
      }
    }

    override def unbind(key: String, value: OtherType): Map[String, String] = {
      val stringValue = value match {
        case CharitableIncorporatedOrganisation => charitableIncorporatedOrganisationKey
        case NonIncorporatedTrust => nonIncorporatedTrustKey
        case RegisteredSociety => registeredSocietyKey
        case UnincorporatedAssociation => unincorporatedAssociationKey
        case Division => divisionKey
        case VatGroup => vatGroupKey
      }
      Map(key -> stringValue)
    }
  }

}