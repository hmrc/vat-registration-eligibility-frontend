package pages

import forms.TurnoverEstimateFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.TurnoverEstimateView

class TurnoverEstimateA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TurnoverEstimateView]
  val form = app.injector.instanceOf[TurnoverEstimateFormProvider]

  "the Turnover Estimate page" when {
    "the page is rendered without errors" when {
      "the user is a partnership" must {
        "pass all accessibility tests" in {
          view(form(), NormalMode, isPartnership = true).toString must passAccessibilityChecks
        }
      }
      "the user isn't a partnership" must {
        "pass all accessibility tests" in {
          view(form(), NormalMode).toString must passAccessibilityChecks
        }
      }
    }
    "the page is rendered with errors" must {
      "pass all accessibility test" in {
        view(form().bind(Map("value" -> "")), NormalMode).toString must passAccessibilityChecks
      }
    }
  }

}
