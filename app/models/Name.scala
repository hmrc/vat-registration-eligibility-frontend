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

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, __}
import utils.Formatters

case class Name(forename: Option[String],
                otherForenames: Option[String],
                surname: String,
                title: Option[String])

object Name {
  implicit val nameFormat = Json.format[Name]

  val normalizeNameReads = (
    (__ \ "forename").readNullable[String](Formatters.normalizeTrimmedHMRCReads) and
      (__ \ "other_forenames").readNullable[String](Formatters.normalizeTrimmedHMRCReads) and
      (__ \ "surname").read[String](Formatters.normalizeTrimmedHMRCReads) and
      (__ \ "title").readNullable[String](Formatters.normalizeTrimmedHMRCReads).map(opt => opt.filter(_.length <= 20))
    )(Name.apply _)
}
