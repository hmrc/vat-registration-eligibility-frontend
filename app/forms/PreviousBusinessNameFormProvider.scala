/*
 * Copyright 2024 HM Revenue & Customs
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
class PreviousBusinessNameFormProvider extends Mappings {

  val previousBusinessNameKey = "previousBusinessName"
  val errorKeyRoot = s"previousBusinessName.error"
  val businessNameRequired = s"$errorKeyRoot.name.required"
  val tooManyChars = s"$errorKeyRoot.name.tooManyChars"
  val specialChars = s"$errorKeyRoot.name.specialChars"
  val regex = """^[A-Za-z0-9 '’‘()\[\]{}<>!«»"ʺ˝ˮ?/\\+=%#*&$€£_\-@¥.,:;]{1,105}$"""
  val maxLength = 105

  def apply(): Form[String] = Form[String](
    single(
      previousBusinessNameKey -> text(businessNameRequired).verifying(firstError(
        maxLength(maxLength, tooManyChars),
        regexp(regex, specialChars))
      )
    )
  )
}