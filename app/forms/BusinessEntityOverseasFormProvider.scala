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
class BusinessEntityOverseasFormProvider extends FormErrorHelper with Mappings {

  val businessEntity: String = "value"
  val businessEntityError: String = "businessEntityOverseas.error.required"

  def apply(): Form[OverseasType] = Form(
    single(
      businessEntity -> of(formatter)
    )
  )

  def formatter: Formatter[OverseasType] = new Formatter[OverseasType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], OverseasType] = {
      data.get(key) match {
        case Some(`netpKey`) => Right(NETP)
        case Some(`overseasKey`) => Right(Overseas)
        case _ => Left(Seq(FormError(key, businessEntityError)))
      }
    }

    override def unbind(key: String, value: OverseasType): Map[String, String] = {
      val stringValue = value match {
        case NETP => netpKey
        case Overseas => overseasKey
      }
      Map(key -> stringValue)
    }
  }

}