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
import models.RegisteringBusiness.{ownBusinessKey, someoneElseKey}
import models.{OwnBusiness, SomeoneElse}
import play.api.data.FormError

class RegisteringBusinessFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "registeringBusiness.error.required"
  val invalidKey = "error.boolean"

  val form = new RegisteringBusinessFormProvider()()

  "RegisteringBusinessFrom" must {

    val fieldName = "value"

    "successfully parse any of the values" in {
      val valueList = Seq(
        (OwnBusiness, ownBusinessKey),
        (SomeoneElse, someoneElseKey)
      )

      valueList.map {
        case (entity, key) =>
          val res = form.bind(Map(fieldName -> key))
          res.value must contain(entity)
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
