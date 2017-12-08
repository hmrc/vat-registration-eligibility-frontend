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

package services

import common.enums.{EligibilityQuestions, EligibilityResult, VatRegStatus}
import connectors.VatRegistrationConnector
import fixtures.VatRegistrationFixture
import helpers.FutureAssertions
import mocks.VatMocks
import models.CurrentProfile
import models.view.Eligibility
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class EligibilityServiceSpec extends UnitSpec with MockitoSugar with VatMocks with FutureAssertions with VatRegistrationFixture {
  implicit val currentProfile = CurrentProfile("Test Me", testRegId, "000-434-1", VatRegStatus.draft,None)
  implicit val hc = HeaderCarrier()
  val validEmptyEligibility = Eligibility(None, None, None, None, None, None)

  class Setup(s4lData: Option[Eligibility] = None, backendData: Option[(String, Int)] = None) {
    val service = new EligibilityService {
      override val s4LConnector = mockS4LConnector
      override val vatRegistrationConnector: VatRegistrationConnector = mockRegConnector
      override val currentVersion = 1
    }

    when(mockS4LConnector.fetchAndGet[Eligibility](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(s4lData))

    getEligibilityMock(Future.successful(backendData))

    when(mockS4LConnector.save(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForS4L {
    val service = new EligibilityService {
      override val s4LConnector = mockS4LConnector
      override val vatRegistrationConnector: VatRegistrationConnector = mockRegConnector
      override val currentVersion = 1

      override def getEligibility(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[Eligibility] =
        Future.successful(validEmptyEligibility)
    }

    when(mockS4LConnector.save(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForBackendSave {
    val service = new EligibilityService {
      override val s4LConnector = mockS4LConnector
      override val vatRegistrationConnector: VatRegistrationConnector = mockRegConnector
      override val currentVersion = 1

      override def getEligibility(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[Eligibility] =
        Future.successful(validEmptyEligibility)
    }

    patchEligibilityMock(Future.successful(Json.toJson("""{}""")))

    when(mockS4LConnector.clear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))
  }

  class SetupForBackendSaveSuccess {
    val service = new EligibilityService {
      override val s4LConnector = mockS4LConnector
      override val vatRegistrationConnector: VatRegistrationConnector = mockRegConnector
      override val currentVersion = 1

      override def getEligibility(implicit currentProfile: CurrentProfile, hc: HeaderCarrier): Future[Eligibility] =
        Future.successful(validEligibility)
    }

    patchEligibilityMock(Future.successful(Json.toJson("""{}""")))

    when(mockS4LConnector.clear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))
  }

  "Calling getEligibility" should {
    val vers = 1
    val results = EligibilityResult(vers).questions

    val partialEligibility = Eligibility(Some(true), Some(false), Some(false), None, None, None)

    "return a default Eligibility view model if nothing is in S4L & backend" in new Setup {
      service.getEligibility returns validEmptyEligibility
    }

    "return a partial Eligibility view model from S4L" in new Setup(Some(partialEligibility)) {
      service.getEligibility returns partialEligibility
    }

    "return a failed noNino Eligibility view model from backend" in new Setup(None, Some((results.noNino, vers))) {
      service.getEligibility returns validEmptyEligibility.copy(hasNino = Some(false))
    }

    "return a failed racehorsesOrLandAndProperty Eligibility view model from backend" in new Setup(None, Some((results.racehorsesOrLandAndProperty, vers))) {
      service.getEligibility returns validEligibility.copy(racehorsesOrLandAndProperty = Some(true))
    }

    "return a full success Eligibility view model from backend" in new Setup(None, Some((results.success, vers))) {
      service.getEligibility returns validEligibility
    }
  }

  "Calling saveQuestions" should {
    import models.view.{YesOrNoQuestion => yn}

    Seq[(yn, Eligibility)](
      (yn(EligibilityQuestions.haveNino, true), validEmptyEligibility.copy(hasNino = Some(true))),
      (yn(EligibilityQuestions.doingBusinessAbroad, false), validEmptyEligibility.copy(internationalBusiness = Some(false))),
      (yn(EligibilityQuestions.doAnyApplyToYou, false), validEmptyEligibility.copy(moreThanOneBusinessOrLegalStatus = Some(false))),
      (yn(EligibilityQuestions.applyingForAnyOf, false), validEmptyEligibility.copy(agriculturalFRSOrAnnualAccount = Some(false))),
      (yn(EligibilityQuestions.applyingForVatExemption, false), validEmptyEligibility.copy(exceptionOrExemption = Some(false))),
      (yn(EligibilityQuestions.companyWillDoAnyOf, false), validEmptyEligibility.copy(racehorsesOrLandAndProperty = Some(false)))
    ).foreach {
      case (view, expected) =>
        s"save to S4L and return an incomplete Eligibility model with ${view.question} value set to ${view.answer}" in new SetupForS4L {
          service.saveEligibility(view) returns expected
        }
    }

    Seq[(yn, Eligibility)](
      (yn(EligibilityQuestions.haveNino, false), validEmptyEligibility.copy(hasNino = Some(false))),
      (yn(EligibilityQuestions.doingBusinessAbroad, true), validEmptyEligibility.copy(internationalBusiness = Some(true))),
      (yn(EligibilityQuestions.doAnyApplyToYou, true), validEmptyEligibility.copy(moreThanOneBusinessOrLegalStatus = Some(true))),
      (yn(EligibilityQuestions.applyingForAnyOf, true), validEmptyEligibility.copy(agriculturalFRSOrAnnualAccount = Some(true))),
      (yn(EligibilityQuestions.applyingForVatExemption, true), validEmptyEligibility.copy(exceptionOrExemption = Some(true))),
      (yn(EligibilityQuestions.companyWillDoAnyOf, true), validEmptyEligibility.copy(racehorsesOrLandAndProperty = Some(true)))
    ).foreach {
      case (view, expected) =>
        s"save to backend and return an Eligibility model with ${view.question} value set to ${view.answer}" in new SetupForBackendSave {
          service.saveEligibility(view) returns expected
        }
    }

    "save to backend a success Eligibility and return view model" in new SetupForBackendSaveSuccess() {
      val view = yn(EligibilityQuestions.companyWillDoAnyOf, false)
      service.saveEligibility(view) returns validEligibility
    }
  }
}
