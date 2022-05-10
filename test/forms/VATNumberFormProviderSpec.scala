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

  val togc = "togc"
  val cole = "cole"
  val vatNumberKey = "vatNumber"
  val errorKeyRoot = "vatNumber.error"
  def vatNumberRequired(togcColeKey: String) = s"$errorKeyRoot.$togcColeKey.required"
  val vatNumberLength = s"$errorKeyRoot.length"
  val nonNumeric = s"$errorKeyRoot.nonNumeric"
  val invalid = s"$errorKeyRoot.invalid"

  s"bind in $togc form" must {
    val form = new VATNumberFormProvider().apply(togc)
    "return errors" when {
      "VAT number is not provided" in {
        form.bind(
          Map(
            vatNumberKey -> ""
          )
        )
          .errors mustBe Seq(FormError(vatNumberKey, vatNumberRequired(togc), Seq()))
      }

      "VAT number exceeds 9 characters" in {
        form.bind(
          Map(
            vatNumberKey -> "01234567890"
          )
        )
          .errors.headOption.map(_.message) mustBe Some(vatNumberLength)
      }

      "VAT number with alphanumeric characters" in {
        form.bind(
          Map(
            vatNumberKey -> "123test89"
          )
        )
          .errors.headOption.map(_.message) mustBe Some(nonNumeric)
      }

      "VAT number is invlaid" in {
        form.bind(
          Map(
            vatNumberKey -> "789666321"
          )
        )
          .errors.headOption.map(_.message) mustBe Some(invalid)
      }
    }

    "returns no errors" when {
      "a valid VAT number is provided" in {
        form.bind(
          Map(
            vatNumberKey -> "011000084"
          )
        ).errors mustBe Nil
      }
    }
  }

  s"bind in $cole form" must {
    val form = new VATNumberFormProvider().apply(cole)
    "return errors" when {
      "VAT number is not provided" in {
        form.bind(
          Map(
            vatNumberKey -> ""
          )
        )
          .errors mustBe Seq(FormError(vatNumberKey, vatNumberRequired(cole), Seq()))
      }

      "VAT number exceeds 9 characters" in {
        form.bind(
          Map(
            vatNumberKey -> "01234567890"
          )
        )
          .errors.headOption.map(_.message) mustBe Some(vatNumberLength)
      }

      "VAT number with alphanumeric characters" in {
        form.bind(
          Map(
            vatNumberKey -> "123test89"
          )
        )
          .errors.headOption.map(_.message) mustBe Some(nonNumeric)
      }

      "VAT number is invlaid" in {
        form.bind(
          Map(
            vatNumberKey -> "789666321"
          )
        )
          .errors.headOption.map(_.message) mustBe Some(invalid)
      }
    }

    "returns no errors" when {
      "a valid VAT number is provided" in {
        form.bind(
          Map(
            vatNumberKey -> "011000084"
          )
        ).errors mustBe Nil
      }
    }
  }
}
