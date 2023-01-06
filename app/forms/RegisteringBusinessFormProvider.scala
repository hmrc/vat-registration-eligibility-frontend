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

package forms

import forms.mappings.Mappings
import models.RegisteringBusiness._
import models._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

import javax.inject.Singleton

@Singleton
class RegisteringBusinessFormProvider extends Mappings {

  val registeringBusiness: String = "value"
  val registeringBusinessError: String = "registeringBusiness.error.required"

  def apply(): Form[RegisteringBusiness] = Form(
    single(
      registeringBusiness -> of(formatter)
    )
  )

  def formatter: Formatter[RegisteringBusiness] = new Formatter[RegisteringBusiness] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RegisteringBusiness] = {
      data.get(key) match {
        case Some(`ownBusinessKey`) => Right(OwnBusiness)
        case Some(`someoneElseKey`) => Right(SomeoneElse)
        case _ => Left(Seq(FormError(key, registeringBusinessError)))
      }
    }

    override def unbind(key: String, value: RegisteringBusiness): Map[String, String] = {
      val stringValue = value match {
        case OwnBusiness => ownBusinessKey
        case SomeoneElse => someoneElseKey
      }
      Map(key -> stringValue)
    }
  }
}
