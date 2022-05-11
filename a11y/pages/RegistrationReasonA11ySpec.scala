package pages

import forms.RegistrationReasonFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.RegistrationReasonView

class RegistrationReasonA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[RegistrationReasonView]
  val form = app.injector.instanceOf[RegistrationReasonFormProvider]

  "the Registration Reason page" when {
    "the page is rendered without errors" when {
      "pass all accessibility tests" in {
        view(form(), NormalMode).toString must passAccessibilityChecks
      }
    }
    "the page is rendered with errors" must {
      "pass all accessibility test" in {
        view(form().bind(Map("value" -> "")), NormalMode).toString must passAccessibilityChecks
      }
    }
  }

}
