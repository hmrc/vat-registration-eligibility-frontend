
package pages

import helpers.A11ySpec
import views.html.InternationalActivityDropout

class InternationalActivityDropoutA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[InternationalActivityDropout]

  "the international activity dropout page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view().toString must passAccessibilityChecks
      }
    }
  }
}