package pages

import forms.ThresholdInTwelveMonthsFormProvider
import helpers.A11ySpec
import models.NormalMode
import services.ThresholdService
import views.html.ThresholdInTwelveMonths

class ThresholdInTwelveMonthsA11ySpec extends A11ySpec with ThresholdService {

  val view = app.injector.instanceOf[ThresholdInTwelveMonths]
  val form = app.injector.instanceOf[ThresholdInTwelveMonthsFormProvider]

  "the Threshold (12 months) page" when {
    "the page is rendered without errors" when {
      "the user is a partnership" must {
        "pass all accessibility tests" in {
          view(form(), NormalMode, isPartnership = true, vatThreshold = formattedVatThreshold()).toString must passAccessibilityChecks
        }
      }
      "the user isn't a partnership" must {
        "pass all accessibility tests" in {
          view(form(), NormalMode, vatThreshold = formattedVatThreshold()).toString must passAccessibilityChecks
        }
      }
    }
    "the page is rendered with errors" must {
      "pass all accessibility test" in {
        view(form().bind(Map("value" -> "")), NormalMode, vatThreshold = formattedVatThreshold()).toString must passAccessibilityChecks
      }
    }
  }

}
