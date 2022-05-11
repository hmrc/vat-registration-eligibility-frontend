package pages

import forms.PreviousBusinessNameFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.PreviousBusinessName

class PreviousBusinessNameA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[PreviousBusinessName]
  val form = app.injector.instanceOf[PreviousBusinessNameFormProvider]

  "the Previous Business Name page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form(), NormalMode, "togc").toString must passAccessibilityChecks
      }
    }
    "the page is rendered with errors" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("previousBusinessName" -> ('w' * 106).toString)), NormalMode, "togc").toString must passAccessibilityChecks
      }
    }
  }

}
