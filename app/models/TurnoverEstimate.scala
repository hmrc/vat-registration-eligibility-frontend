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

package models

import play.api.libs.json._
import utils.{Enumerable, RadioOption, WithName}

sealed trait TurnoverEstimate

case class TurnoverEstimateFormElement(value : String)

object TurnoverEstimateFormElement {
  implicit val turnoverEstimateFormReads: Reads[TurnoverEstimateFormElement] = new Reads[TurnoverEstimateFormElement] {
    override def reads(json: JsValue): JsResult[TurnoverEstimateFormElement] = {
      val amount = (json \ "amount").as[String]

      JsSuccess(TurnoverEstimateFormElement(amount))
    }
  }

  implicit val turnoverEstimateFormWrites: Writes[TurnoverEstimateFormElement] = new Writes[TurnoverEstimateFormElement] {
    override def writes(formEle: TurnoverEstimateFormElement): JsValue = {
      val amount = Json.obj("amount" -> formEle.value)

      Json.obj("selection" -> formEle.value).deepMerge(amount)
    }
  }
}
