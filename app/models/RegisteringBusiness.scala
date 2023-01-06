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

package models

import play.api.libs.json._

sealed trait RegisteringBusiness

case object OwnBusiness extends RegisteringBusiness

case object SomeoneElse extends RegisteringBusiness

object RegisteringBusiness {
  val ownBusinessKey = "own"
  val someoneElseKey = "someone-else"

  implicit def writes[T <: RegisteringBusiness]: Writes[T] = Writes[T] {
    case OwnBusiness => JsString(ownBusinessKey)
    case SomeoneElse => JsString(someoneElseKey)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Registering Business Reason: $unknownKey")
  }

  def registeringBusinessToString(registeringBusiness: RegisteringBusiness) = registeringBusiness match {
    case OwnBusiness => "registeringBusiness.radioOwn"
    case SomeoneElse => "registeringBusiness.radioElse"
  }

  implicit val jsonReads: Reads[RegisteringBusiness] = Reads[RegisteringBusiness] {
    case JsString(`ownBusinessKey`) | JsBoolean(true) => JsSuccess(OwnBusiness) //TODO Remove JsBoolean cases 2 weeks from merge as they're to fix old data
    case JsString(`someoneElseKey`) | JsBoolean(false) => JsSuccess(SomeoneElse)
    case unknownKey => throw new IllegalArgumentException(s"Unknown Registering Business Reason: $unknownKey")
  }

  implicit val jsonFormat: Format[RegisteringBusiness] = Format[RegisteringBusiness](jsonReads, writes)
}
