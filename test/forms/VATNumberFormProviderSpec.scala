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

class VATNumberFormProviderSpec extends BooleanFieldBehaviours {
  val form = new VATNumberFormProvider().apply()

  val vatNumberKey = "vatNumber"
  val errorKeyRoot = "vatNumber.error"
  val vatNumberRequired = s"$errorKeyRoot.togc.required"
  val vatNumberLength = s"$errorKeyRoot.length"
  val nonNumeric = s"$errorKeyRoot.nonNumeric"
  val invalid = s"$errorKeyRoot.invalid"

  "bind" should {
    "return errors" when {
      "VAT number is not provided" in {
        form.bind(
          Map(
            vatNumberKey -> ""
          )
        )
          .errors shouldBe Seq(FormError(vatNumberKey, vatNumberRequired, Seq()))
      }

      "VAT number exceeds 9 characters" in {
        form.bind(
          Map(
            vatNumberKey -> "01234567890"
          )
        )
          .errors.headOption.map(_.message) shouldBe Some(vatNumberLength)
      }

      "VAT number with alphanumeric characters" in {
        form.bind(
          Map(
            vatNumberKey -> "123test89"
          )
        )
          .errors.headOption.map(_.message) shouldBe Some(nonNumeric)
      }

      "VAT number is invlaid" in {
        form.bind(
          Map(
            vatNumberKey -> "789666321"
          )
        )
          .errors.headOption.map(_.message) shouldBe Some(invalid)
      }
    }

    "returns no errors" when {
      "a valid VAT number is provided" in {
        form.bind(
          Map(
            vatNumberKey -> "011000084"
          )
        ).errors shouldBe Nil
      }
    }
  }
}
