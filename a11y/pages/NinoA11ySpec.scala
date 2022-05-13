
package pages

import forms.NinoFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.Nino

class NinoA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Nino]
  val form = app.injector.instanceOf[NinoFormProvider]

  "the nino page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form(), NormalMode).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing form value" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("value" -> "")), NormalMode).toString must passAccessibilityChecks
      }
    }
  }
}