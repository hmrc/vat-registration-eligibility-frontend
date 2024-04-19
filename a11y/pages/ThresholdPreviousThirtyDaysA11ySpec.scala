package pages

import forms.ThresholdPreviousThirtyDaysFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.ThresholdPreviousThirtyDays
import services.ThresholdService

class ThresholdPreviousThirtyDaysA11ySpec extends A11ySpec with ThresholdService {

  val view = app.injector.instanceOf[ThresholdPreviousThirtyDays]
  val form = app.injector.instanceOf[ThresholdPreviousThirtyDaysFormProvider]

  "the Threshold (previous 30 Days) page" when {
    "the page is rendered without errors" when {
      "the user is a partnership" must {
        "pass all accessibility tests" in {
          view(form(""), NormalMode, isPartnership = true, vatThreshold = formattedVatThreshold()).toString must passAccessibilityChecks
        }
      }
      "the user isn't a partnership" must {
        "pass all accessibility tests" in {
          view(form(""), NormalMode, vatThreshold = formattedVatThreshold()).toString must passAccessibilityChecks
        }
      }
    }
    "the page is rendered with errors" must {
      "pass all accessibility test" in {
        view(form("").bind(Map("value" -> "")), NormalMode, vatThreshold = formattedVatThreshold()).toString must passAccessibilityChecks
      }
    }
  }

}
