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

package helpers

import org.jsoup.nodes.Document

import java.time.LocalDate

trait FieldValueChecker {

  val checkedAttr = "checked"
  val valueAttr = "value"

  private object Selectors {
    private def baseInputSelector(inputType: String, id: String) = s"input[type=$inputType][id=$id]"
    def radio(id: String): String = baseInputSelector("radio", id)
    def textBox(id:String): String =  baseInputSelector("text", id)
    def dateFieldDay(id: String) = baseInputSelector("text", s"$id.day")
    def dateFieldMonth(id: String) = baseInputSelector("text", s"$id.month")
    def dateFieldYear(id: String) = baseInputSelector("text", s"$id.year")
  }

  implicit class EnhancedDoc(doc: Document) {
    def radioIsSelected(id: String): Boolean =
      doc.select(Selectors.radio(id)).hasAttr(checkedAttr)

    def textboxContainsValue(id: String, expectedValue: String): Boolean =
      doc.select(Selectors.textBox(id)).`val`() == expectedValue

    def dateFieldContainsValue(id: String, expectedValue: LocalDate, includeDay: Boolean = true): Boolean =
      { if (includeDay) doc.select(Selectors.dateFieldDay(id)).attr(valueAttr) == expectedValue.getDayOfMonth.toString else true } && {
        doc.select(Selectors.dateFieldMonth(id)).attr(valueAttr) == expectedValue.getMonthValue.toString &&
        doc.select(Selectors.dateFieldYear(id)).attr(valueAttr) == expectedValue.getYear.toString
      }
  }

}
