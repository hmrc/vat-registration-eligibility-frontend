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

package controllers

import helpers.IntegrationSpecBase
import identifiers._
import models._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class FixedEstablishmentControllerISpec extends IntegrationSpecBase {

  val pageUrl  = "/fixed-establishment"
  val yesRadio = "fixedEstablishment-yes"
  val noRadio  = "fixedEstablishment-no"

  "GET /fixed-establishment" when {

    "FixedEstablishmentExperiment is disabled" when {

      "an answer exists for the page" must {

        "return OK with the answer pre=populated" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData(sessionIdStr, FixedEstablishmentId, true)

          val res = await(buildClient(pageUrl).get())
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(yesRadio) mustBe true
          doc.radioIsSelected(noRadio) mustBe false
        }
      }

      "an answer doesn't exist for the page" must {
        "return OK with an empty form" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient(pageUrl).get())
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(yesRadio) mustBe false
          doc.radioIsSelected(noRadio) mustBe false
        }
      }
    }

  }

  "POST /fixed-establishment" when {
    "there is no prior data" when {
      "the user answers 'Yes'" must {
        "redirect to the Business Entity page" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
        }
      }

      "the user answers 'No'" when {
        "redirect to the Business Entity Overseas page" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
        }
      }
    }

    "there is non-conflicting prior data" when {
      "the user answers 'Yes' and had previously answered 'Yes'" must {
        "redirect to the Business Entity page and do not clear existing answers" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          setUpExistingJourneyData()
          cacheSessionData(sessionIdStr, FixedEstablishmentId, true)
          checkNumberOfDocumentsInSession() mustBe 16

          val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
          checkNumberOfDocumentsInSession() mustBe 16
        }
      }

      "the user answers 'No' and had previously answered 'No'" must {
        "redirect to the Business Entity page and do not clear existing answers" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          setUpExistingJourneyData()
          cacheSessionData(sessionIdStr, FixedEstablishmentId, false)
          checkNumberOfDocumentsInSession() mustBe 16

          val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
          checkNumberOfDocumentsInSession() mustBe 16
        }
      }
    }

    "there is conflicting prior data" when {
      "the user answers 'Yes' but had previously answered 'No'" must {
        "redirect to the Business Entity page and clear old answers" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          setUpExistingJourneyData()
          cacheSessionData(sessionIdStr, FixedEstablishmentId, false)
          checkNumberOfDocumentsInSession() mustBe 16

          val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
          checkNumberOfDocumentsInSession() mustBe 2
        }
      }

      "the user answers 'No' but had previously answered 'Yes'" must {
        "redirect to the Business Entity page and clear old answers" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          setUpExistingJourneyData()
          cacheSessionData(sessionIdStr, FixedEstablishmentId, true)
          checkNumberOfDocumentsInSession() mustBe 16

          val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
          checkNumberOfDocumentsInSession() mustBe 2
        }
      }
    }

    "the user doesn't answer" must {
      "return BAS_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }

  }

  private def setUpExistingJourneyData(): Unit = {
    cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, UKCompany)
    cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityPartnershipId, GeneralPartnership)
    cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityOtherId, NonIncorporatedTrust)
    cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityOverseasId, Overseas)
    cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(LocalDate.now())))
    cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
    cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
    cacheSessionData[Boolean](sessionIdStr, VoluntaryRegistrationId, true)
    cacheSessionData[DateFormElement](sessionIdStr, DateOfBusinessTransferId, DateFormElement(LocalDate.now()))
    cacheSessionData[String](sessionIdStr, PreviousBusinessNameId, "test")
    cacheSessionData[String](sessionIdStr, VATNumberId, "test")
    cacheSessionData[Boolean](sessionIdStr, KeepOldVrnId, true)
    cacheSessionData[Boolean](sessionIdStr, TermsAndConditionsId, true)
    cacheSessionData[Boolean](sessionIdStr, TaxableSuppliesInUkId, true)
    cacheSessionData[DateFormElement](sessionIdStr, ThresholdTaxableSuppliesId, DateFormElement(LocalDate.now()))
  }

  private def checkNumberOfDocumentsInSession(): Int =
    getAllSessionData(sessionIdStr)
      .map(_.size)
      .getOrElse(0)

}
