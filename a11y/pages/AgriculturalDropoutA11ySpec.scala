
package pages

import helpers.A11ySpec
import views.html.AgriculturalDropout

class AgriculturalDropoutA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[AgriculturalDropout]

  "the agricultural dropout page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view().toString must passAccessibilityChecks
      }
    }
  }

}
