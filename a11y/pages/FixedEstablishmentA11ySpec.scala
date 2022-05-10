
package pages

import forms.FixedEstablishmentFormProvider
import helpers.A11ySpec
import views.html.FixedEstablishment

class FixedEstablishmentA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[FixedEstablishment]
  val form = app.injector.instanceOf[FixedEstablishmentFormProvider]

  "the Application Reference page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form()).toString must passAccessibilityChecks
      }
    }
  }

}
