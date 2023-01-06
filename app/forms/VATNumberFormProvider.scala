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
import play.api.data.Form
import play.api.data.Forms.single

import javax.inject.Singleton

@Singleton
class VATNumberFormProvider extends FormErrorHelper with Mappings {

  val vatNumberKey = "vatNumber"
  val errorKeyRoot = s"vatNumber.error"
  val vatNumberLength = s"$errorKeyRoot.length"
  val nonNumeric = s"$errorKeyRoot.nonNumeric"
  val invalid = s"$errorKeyRoot.invalid"
  val regex = """^[0-9]{9}$"""
  val length = 9

  def apply(togcColeKey: String): Form[String] = {
    val vatNumberRequired = s"$errorKeyRoot.$togcColeKey.required"
    Form[String](
      single(
        vatNumberKey -> text(vatNumberRequired).verifying(
          stopOnFail(
            minLength(length, vatNumberLength),
            maxLength(length, vatNumberLength),
            regexp(regex, nonNumeric),
            isValidChecksum(invalid)
          )
        )
      )
    )
  }
}
