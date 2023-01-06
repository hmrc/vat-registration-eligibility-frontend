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

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PreviousBusinessNameFormProviderSpec extends BooleanFieldBehaviours {

  val form = new PreviousBusinessNameFormProvider().apply()

  "bind" must {

    val previousBusinessNameKey = "previousBusinessName"
    val errorKeyRoot = s"previousBusinessName.error"
    val businessNameRequired = s"$errorKeyRoot.name.required"
    val tooManyChars = s"$errorKeyRoot.name.tooManyChars"
    val specialChars = s"$errorKeyRoot.name.specialChars"

    "return errors" when {

      "a business name is not provided" in {
        form.bind(
          Map(
            previousBusinessNameKey -> ""
          )
        )
          .errors mustBe Seq(FormError(previousBusinessNameKey, businessNameRequired))
      }

      "the business name provided exceeds 105 characters" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd"
          )
        ).errors.headOption.map(_.message) mustBe Some(tooManyChars)
      }

      "the business name provided includes special characters that are invalid" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "^`|~"
          )
        ).errors.headOption.map(_.message) mustBe Some(specialChars)
      }
    }

    "return no errors" when {
      "a business name is provided in the correct format" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "Al Pacino Ltd"
          )
        ).errors mustBe Nil
      }

      "the business name provided includes special characters that are valid" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "#!$ %&' *+ -/=? _{}"
          )
        ).errors mustBe Nil
      }
    }
  }
}