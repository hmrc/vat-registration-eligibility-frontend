package pages

import helpers.A11ySpec
import views.html.TermsAndConditions

class TermsAndConditionsA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TermsAndConditions]

  "the Terms And Conditions page" must {
    "pass all accessibility tests" in {
      view()(request, messages, config).toString must passAccessibilityChecks
    }
  }


}
