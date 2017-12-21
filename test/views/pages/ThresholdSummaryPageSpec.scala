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

package views.pages

import java.time.LocalDate

import controllers.builders.SummaryVatThresholdBuilder
import fixtures.VatRegistrationFixture
import models.MonthYearModel
import models.view.{OverThresholdView, Summary}
import org.jsoup.Jsoup
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.pages.threshold_summary


class ThresholdSummaryPageSpec extends UnitSpec with WithFakeApplication with I18nSupport with VatRegistrationFixture {

  val injector = fakeApplication.injector
  implicit val messagesApi = injector.instanceOf[MessagesApi]
  implicit val request = FakeRequest()

  val incorpDate = MonthYearModel.FORMAT_DD_MMMM_Y.format(LocalDate.of(2017,7,8))

  "Rendering the summary page for a post incorporprated company that has gone over threshold" should{
    val data = validThresholdPostIncorp.copy(overThreshold = Some(OverThresholdView(true, Some(LocalDate.of(2017,8,5)))))
    val summarySection = SummaryVatThresholdBuilder(data).section
    lazy val view = threshold_summary(Summary(Seq(summarySection)),incorpDate)
    lazy val document = Jsoup.parse(view.body)


    "display the right heading" in {
      document.getElementById("pageHeading").text shouldBe messagesApi("pages.thresholdSummary.heading")
    }
    "display threshold selection summary with the right incorp date" in {
      document.getElementById("threshold.overThresholdSelectionQuestion").text shouldBe messagesApi("pages.summary.threshold.overThresholdSelection", incorpDate)
    }
    "display the post incorp selection answer as yes" in {
      document.getElementById("threshold.overThresholdSelectionAnswer").text shouldBe "Yes"
    }
    "display the vat over threshold date as August 2017" in {
      document.getElementById("threshold.overThresholdDateAnswer").text shouldBe "August 2017"
    }
  }

  "Rendering the summary page for a post incorporated company that hasn't gone over" should{
    val summarySection = SummaryVatThresholdBuilder(validThresholdPostIncorp).section
    lazy val view = threshold_summary(Summary(Seq(summarySection)),incorpDate)
    lazy val document = Jsoup.parse(view.body)


    "display the right heading" in {
      document.getElementById("pageHeading").text shouldBe messagesApi("pages.thresholdSummary.heading")
    }
    "display threshold selection summary with the right incorp date" in {
      document.getElementById("threshold.overThresholdSelectionQuestion").text shouldBe messagesApi("pages.summary.threshold.overThresholdSelection", incorpDate)
    }
    "display the post incorp selection answer as Noo" in {
      document.getElementById("threshold.overThresholdSelectionAnswer").text shouldBe "No"
    }
    "the date row should not be shown" in {
      document.getElementById("threshold.overThresholdDate") shouldBe null
    }
  }
}
