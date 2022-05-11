package pages

import forms.RegisteringBusinessFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.RegisteringBusinessView

class RegisteringBusinessA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[RegisteringBusinessView]
  val form = app.injector.instanceOf[RegisteringBusinessFormProvider]

  "the Registering Business page" when {
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
