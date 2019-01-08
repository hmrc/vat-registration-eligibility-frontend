/*
 * Copyright 2019 HM Revenue & Customs
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

package utils

import java.time.LocalDate

import org.joda.time.{LocalDate => JodaLocalDate}
import uk.gov.hmrc.time.DateTimeUtils


object VATDateHelper {

  private val convertFromJavaToJoda = (date: LocalDate) => new JodaLocalDate(date.getYear,date.getMonthValue,date.getDayOfMonth)

  val dateEqualOrAfter201741 = (l:LocalDate) => DateTimeUtils.isEqualOrAfter(new JodaLocalDate(2017,4,1), convertFromJavaToJoda(l))

  val dateBefore201741After2016331 = (l:LocalDate) => {
    val jDate = convertFromJavaToJoda(l)
    jDate.isBefore(new JodaLocalDate(2017,4,1)) && jDate.isAfter(new JodaLocalDate(2016,3,31))
  }
  val dateBefore201641After2015331 = (l:LocalDate) => {
    val jDate = convertFromJavaToJoda(l)
    jDate.isBefore(new JodaLocalDate(2016,4,1)) && jDate.isAfter(new JodaLocalDate(2015,3,31))
  }
  val lessThan12Months = (l:LocalDate) => {
    val jDate = convertFromJavaToJoda(l)
    jDate.isAfter(new JodaLocalDate(JodaLocalDate.now.minusMonths(12)))
  }
}