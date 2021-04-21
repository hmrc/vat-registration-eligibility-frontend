/*
 * Copyright 2021 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec

import java.time.LocalDate

object VATDateHelperSpec extends PlaySpec{
  "dateEqualOrAfter201741" should {
    List(
    ("return true if date > 201741", LocalDate.of(2017,4,2), true),
    ("return true if date = 201741", LocalDate.of(2017,4,1), true),
    ("return false if date < 201741", LocalDate.of(2017,3,31),false)).foreach { test =>
    val (cond, date, bool) = test
    cond in {
      VATDateHelper.dateEqualOrAfter201741(date) mustBe bool
    }
   }
  }
  "dateBefore201741After2016331" should {
    List(
      ("return true if date < 201741 & > 2016331", LocalDate.of(2017,3,30), true),
      ("return false date = 2016331",LocalDate.of(2016,3,31), false),
      ("return false date = 201741", LocalDate.of(2017,4,1),false),
      ("return false date > 201741", LocalDate.of(2017,4,2),false)).foreach { test =>
      val (cond, date, bool) = test
      cond in {
        VATDateHelper.dateBefore201741After2016331(date) mustBe bool
      }
    }
  }
  "dateBefore201641After2015331" should {
    List(
      ("return true if date < 201641 & > 2015331", LocalDate.of(2017,3,30), true),
      ("return false date = 2015331",LocalDate.of(2015,3,31), false),
      ("return false date = 201641", LocalDate.of(2016,4,1),false),
      ("return false date > 201641", LocalDate.of(2016,4,2),false)).foreach { test =>
      val (cond, date, bool) = test
      cond in {
        VATDateHelper.dateBefore201741After2016331(date) mustBe bool
      }
    }
  }
}
