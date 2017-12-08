/*
 * Copyright 2017 HM Revenue & Customs
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

package models.view

import play.api.libs.json.{JsValue, Json, Writes}
import models.view.VoluntaryRegistration.REGISTER_NO

case class Threshold(taxableTurnover: Option[TaxableTurnover],
                     voluntaryRegistration: Option[VoluntaryRegistration],
                     voluntaryRegistrationReason: Option[VoluntaryRegistrationReason],
                     overThreshold: Option[OverThresholdView],
                     expectationOverThreshold: Option[ExpectationOverThresholdView])

object Threshold{
  implicit val format = Json.format[Threshold]

  val apiWrites: Writes[Threshold] = new Writes[Threshold] {
    override def writes(o: Threshold): JsValue = {
      val mandatoryRegitration = o.voluntaryRegistration
                                  .map(_.yesNo == REGISTER_NO)
                                  .getOrElse(throw new IllegalStateException("Missing Voluntary Registration value to save into backend"))

      val voluntaryRegistrationReason = o.voluntaryRegistrationReason.fold(Json.obj())(v => Json.obj("voluntaryReason" -> v.reason))
      val overThresholdDate = o.overThreshold.fold(Json.obj()) { overThreshold =>
        if (overThreshold.selection) {
          Json.obj("overThresholdDate" -> overThreshold.date.getOrElse(throw new IllegalStateException("Missing overThreshold date to save into backend")))
        } else {
          Json.obj()
        }
      }
      val expectedOverThresholdDate = o.expectationOverThreshold.fold(Json.obj()) { expectedOverThreshold =>
        if (expectedOverThreshold.selection) {
          Json.obj("expectedOverThresholdDate" -> expectedOverThreshold.date.getOrElse(throw new IllegalStateException("Missing expectedOverThreshold date to save into backend")))
        } else {
          Json.obj()
        }
      }

      Json.obj("mandatoryRegistration" -> mandatoryRegitration) ++ voluntaryRegistrationReason ++ overThresholdDate ++ expectedOverThresholdDate
    }
  }
}
