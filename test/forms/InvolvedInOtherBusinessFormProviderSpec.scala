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

import featureswitch.core.config.{FeatureSwitching, OBIFlow, TOGCFlow}
import forms.behaviours.BooleanFieldBehaviours
import play.api.data.{Form, FormError}

class InvolvedInOtherBusinessFormProviderSpec extends BooleanFieldBehaviours with FeatureSwitching {

  val requiredKey = "involvedInOtherBusiness.error.required"
  val requiredObiKey = "involvedInOtherBusiness.error.required.obi"
  val requiredTogcKey = "involvedInOtherBusiness.error.required.takingOver"
  val requiredVatGroupKey = "involvedInOtherBusiness.error.required.vatGroup"
  val invalidKey = "error.boolean"
  val fieldName = "value"

  def form: Form[Boolean] = new InvolvedInOtherBusinessFormProvider().form

  ".value" when {
    "no feature switches are set" must {
      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }

    "TOGC FS is set" must {
      enable(TOGCFlow)
      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredObiKey)
      )
      disable(TOGCFlow)
    }

    "OBI FS is set" must {
      enable(OBIFlow)
      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredTogcKey)
      )
      disable(OBIFlow)
    }

    "OBI and TOGC FS is set" must {
      enable(TOGCFlow)
      enable(OBIFlow)
      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredVatGroupKey)
      )
      disable(TOGCFlow)
      disable(OBIFlow)
    }
  }
}
