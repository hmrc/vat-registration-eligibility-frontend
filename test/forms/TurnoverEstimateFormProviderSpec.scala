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

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class TurnoverEstimateFormProviderSpec extends BooleanFieldBehaviours {

  val form = new TurnoverEstimateFormProvider().apply()

  "bind" should {
    val amountFieldName = s"turnoverEstimateAmount"
    val errorKeyRoot = s"turnoverEstimate.error"
    val turnoverEstimateRequired = s"$errorKeyRoot.required"
    val turnoverEstimateAmountLessThan = s"$errorKeyRoot.amount.giveLessThan"
    val turnoverEstimateAmountMoreThan = s"$errorKeyRoot.amount.giveMoreThan"
    val turnoverEstimateNeedsNumbers = s"$errorKeyRoot.amount.numbers"

    "return errors" when {

      "no amount is provided" in {
        form.bind(
          Map(
            amountFieldName -> ""
          )
        )
          .errors shouldBe Seq(FormError(amountFieldName, turnoverEstimateRequired, Seq()))
      }

      "an invalid amount is provided" in {
        form.bind(
          Map(
            amountFieldName -> "abcd"
          )
        ).errors shouldBe Seq(FormError(amountFieldName, turnoverEstimateNeedsNumbers))
      }

      "the amount is too low" in {
        val wrappedArray = Seq(BigInt("-1"))
        form.bind(
          Map(
            amountFieldName -> "-100"
          )
        ).errors shouldBe Seq(FormError(amountFieldName, turnoverEstimateAmountMoreThan, wrappedArray))
      }

      "the amount is too high" in {
        val wrappedArray = Seq(BigInt("999999999999999"))
        form.bind(
          Map(
            amountFieldName -> "99999999999999999999999999"
          )
        ).errors shouldBe Seq(FormError(amountFieldName, turnoverEstimateAmountLessThan, wrappedArray))
      }
    }
    "should return no errors" when {
      "the amount is zero" in {
        form.bind(
          Map(
            amountFieldName -> "0"
          )
        ).errors shouldBe Nil
      }
      "the amount is valid" in {
        form.bind(
            Map(
              amountFieldName -> "12000"
        )
        ).errors shouldBe Nil
      }
    }
  }
}
