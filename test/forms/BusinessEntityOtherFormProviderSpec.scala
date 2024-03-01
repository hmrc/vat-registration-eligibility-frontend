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

import base.SpecBase
import models.BusinessEntity._
import models._
import play.api.data.FormError

class BusinessEntityOtherFormProviderSpec extends SpecBase {
  val form = new BusinessEntityOtherFormProvider()()

  "businessEntityOtherForm" must {

    val businessEntity = "value"

    val businessEntityErrorKey = "businessEntityOther.error.required"

    "successfully parse any of the entities" in {
      val entityList = Seq(
        (CharitableIncorporatedOrganisation, charitableIncorporatedOrganisationKey),
        (NonIncorporatedTrust, nonIncorporatedTrustKey),
        (RegisteredSociety, registeredSocietyKey),
        (UnincorporatedAssociation, unincorporatedAssociationKey),
        (Division, divisionKey),
        (Overseas, overseasKey)
      )

      entityList.map {
        case (entity, key) =>
          val res = form.bind(Map(businessEntity -> key))
          res.value must contain(entity)
      }
    }

    "fail when nothing has been entered in the view" in {
      val res = form.bind(Map.empty[String, String])
      res.errors must contain(FormError(businessEntity, businessEntityErrorKey))
    }

    "fail when it is not an expected value in the view" in {
      val res = form.bind(Map(businessEntity -> "invalid"))
      res.errors must contain(FormError(businessEntity, businessEntityErrorKey))
    }
  }
}

