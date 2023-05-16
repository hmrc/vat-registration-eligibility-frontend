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

package utils

import base.SpecBase
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.div

class OptimizelyHelperSpec extends SpecBase with Viewtils {

  lazy val helper: OptimizelyHelper = app.injector.instanceOf[OptimizelyHelper]
  lazy val div: div = app.injector.instanceOf[div]

  val experimentId = "test-experiment"

  def testContent(index: String = "a"): Html = Html(s"test-html-$index")

  def testDiv(index: String = "a"): Html = div(content = testContent(index), id = Some(s"$experimentId-$index"), classes = Some("govuk-!-display-none"))

  val testControlDiv: Html = div(content = testContent(), id = Some(s"$experimentId-control"))
  val multipleTestVariants: Seq[Html] = ('a' to 'e') map (idx => testContent(idx.toString))
  val multipleTestDivs: Html = html(testDiv(), testDiv("b"), testDiv("c"), testDiv("d"), testDiv("e"))

  implicit val request = FakeRequest()

  "experimentDiv" must {

    "render html wrapped in div" in {

      val expectedResult = div(content = testContent(), id = Some(experimentId))
      val actualResult = helper.experimentDiv(experimentId, testContent())

      expectedResult mustBe actualResult
    }
  }

  "controlDiv" must {

    "render html wrapped in div" in {

      val expectedResult = testControlDiv
      val actualResult = helper.controlDiv(experimentId, testContent())

      expectedResult mustBe actualResult
    }
  }

  "variantDiv" must {

    "render html wrapped in div with govuk-!-display-none class" in {

      val expectedResult = div(content = testContent(), id = Some(s"$experimentId-b"), classes = Some("govuk-!-display-none"))
      val actualResult = helper.variantDiv(experimentId, "b", testContent())

      expectedResult mustBe actualResult
    }
  }

  "variantDivs" when {

    "no variants are provided" must {

      "render empty html" in {

        val expectedResult = HtmlFormat.empty
        val actualResult = helper.variantDivs(experimentId)

        expectedResult mustBe actualResult
      }
    }

    "one variant is provided" must {

      "render html with a single div with govuk-!-display-none class" in {

        val expectedResult = div(content = testContent(), id = Some(s"$experimentId-a"), classes = Some("govuk-!-display-none"))
        val actualResult = helper.variantDivs(experimentId, testContent())

        expectedResult mustBe actualResult
      }
    }

    "multiple variants are provided" must {

      "render html with a single div with govuk-!-display-none class" in {

        val expectedResult = multipleTestDivs
        val actualResult = helper.variantDivs(experimentId, multipleTestVariants: _*)

        expectedResult mustBe actualResult
      }
    }
  }

  "render" when {

    "enabled is true" when {

      "there are no variants" must {

        "render control div only" in {

          val expectedResult =
            html(
              div(
                id = Some(experimentId),
                content =
                  html(
                    testControlDiv
                  )
              )
            )

          val actualResult = helper.render(
            experimentId = experimentId,
            isEnabled = true,
            control = testContent()
          )

          expectedResult mustBe actualResult
        }
      }

      "there is one variant" must {

        "render control div and variant with govuk-!-display-none class" in {

          val expectedResult =
            html(
              div(
                id = Some(experimentId),
                content =
                  html(
                    testControlDiv,
                    testDiv()
                  )
              )
            )

          val actualResult = helper.render(
            experimentId = experimentId,
            isEnabled = true,
            control = testContent(),
            variants = testContent()
          )

          expectedResult mustBe actualResult
        }
      }

      "there are multiple variants" must {

        "render control div and all variants with govuk-!-display-none class" in {

          val expectedResult =
            html(
              div(
                id = Some(experimentId),
                content =
                  html(
                    testControlDiv,
                    multipleTestDivs
                  )
              )
            )

          val actualResult = helper.render(
            experimentId = experimentId,
            isEnabled = true,
            control = testContent(),
            variants = multipleTestVariants: _*
          )

          expectedResult mustBe actualResult

        }
      }
    }

    "enabled is false" when {

      "render control div only" in {

        val expectedResult =
          html(
            div(
              id = Some(experimentId),
              content = testContent()
            )
          )

        val actualResult = helper.render(
          experimentId = experimentId,
          isEnabled = false,
          control = testContent(),
          variants = multipleTestVariants: _*
        )

        expectedResult mustBe actualResult

      }
    }
  }

  "letterAtIndex" must {

    "return the letter as a string which corresponds to its index" in {

      val expectedResult = ('a' to 'z').map(_.toString)
      val actualResult = (0 to 25).map(helper.letterAtIndex)

      expectedResult mustBe actualResult
    }
  }
}
