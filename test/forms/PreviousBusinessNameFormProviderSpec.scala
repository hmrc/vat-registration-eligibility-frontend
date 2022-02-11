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

package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PreviousBusinessNameFormProviderSpec extends BooleanFieldBehaviours {

  val form = new PreviousBusinessNameFormProvider().apply()

  "bind" should {

    val previousBusinessNameKey = "previousBusinessName"
    val errorKeyRoot = s"previousBusinessName.error"
    val businessNameRequired = s"$errorKeyRoot.name.required"
    val tooManyChars = s"$errorKeyRoot.name.tooManyChars"
    val specialChars = s"$errorKeyRoot.name.specialChars"

    "should return errors" when {

      "a business name is not provided" in {
        form.bind(
          Map(
            previousBusinessNameKey -> ""
          )
        )
          .errors shouldBe Seq(FormError(previousBusinessNameKey, businessNameRequired))
      }

      "the business name provided exceeds 105 characters" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd Al Pacino Ltd"
          )
        ).errors.headOption.map(_.message) shouldBe Some(tooManyChars)
      }

      "the business name provided includes special characters that are invalid" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "^`|~"
          )
        ).errors.headOption.map(_.message) shouldBe Some(specialChars)
      }
    }

    "should return no errors" when {
      "a business name is provided in the correct format" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "Al Pacino Ltd"
          )
        ).errors shouldBe Nil
      }

      "the business name provided includes special characters that are valid" in {
        form.bind(
          Map(
            previousBusinessNameKey -> "#!$ %&' *+ -/=? _{}"
          )
        ).errors shouldBe Nil
      }
    }
  }
}