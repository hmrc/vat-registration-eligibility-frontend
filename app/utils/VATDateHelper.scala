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

package utils

import java.time.LocalDate

import org.joda.time.{LocalDate => JodaLocalDate}

object VATDateHelper {

  // TODO make these checks part of threshold service if they're still required at all
  private val firstApril2017 = LocalDate.of(2017,4,1)
  private val firstApril2016 = LocalDate.of(2016, 4, 1)
  private val endMarch2016 = LocalDate.of(2016,3,31)
  private val endMarch2015 = LocalDate.of(2015, 3, 31)
  private val twelveMonthsAgo = LocalDate.now.minusYears(1)

  val dateEqualOrAfter201741 = (date: LocalDate) => !date.isBefore(firstApril2017)

  val dateBefore201741After2016331 = (date: LocalDate) => date.isBefore(firstApril2017) && date.isAfter(endMarch2016)

  val dateBefore201641After2015331 = (date: LocalDate) => date.isBefore(firstApril2016) && date.isAfter(endMarch2015)

  val lessThan12Months = (date: LocalDate) => date.isAfter(twelveMonthsAgo)

}