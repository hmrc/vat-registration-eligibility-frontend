/*
 * Copyright 2018 HM Revenue & Customs
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

package views.pages

import java.time.LocalDate

import forms.{ExpectationThresholdForm}
import models.view.{ExpectationOverThresholdView}
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.Injector
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.pages.{expectation_over_threshold => expectedOverThresholdPage}


class ExpectedOverThresholdPageSpec extends UnitSpec with WithFakeApplication with I18nSupport{
  implicit val request = FakeRequest()
  val injector : Injector = fakeApplication.injector
  implicit val messagesApi : MessagesApi = injector.instanceOf[MessagesApi]

  val incorpDate = LocalDate.of(2016, 8, 5)


  val model = ExpectationOverThresholdView(true,Some(LocalDate.of(2017,1,1)))
  val form = ExpectationThresholdForm.form(incorpDate).fill(model)

  lazy val view = expectedOverThresholdPage(form)
  lazy val document = Jsoup.parse(view.body)

  "Expected Over threshold page" should {
    "display the correct title" in {
      document.getElementById("pageHeading").text shouldBe "In the past, have you ever thought the company's VAT-taxable sales would go over the threshold in a 30-day period?"
    }
  }
  "pre-select the correct radio" in {
    document.getElementById(s"${ExpectationThresholdForm.RADIO_YES_NO}-true").attr("checked") shouldBe "checked"
  }
  "prepopulate the correct fields" in {
    document.getElementsByClass("form-control").get(0).attr("value") shouldBe "1"
    document.getElementsByClass("form-control").get(1).attr("value") shouldBe "1"
    document.getElementsByClass("form-control").get(2).attr("value") shouldBe "2017"
  }

}
