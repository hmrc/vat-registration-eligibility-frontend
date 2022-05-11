package pages

import helpers.A11ySpec
import views.html.Unauthorised

class UnauthorisedA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Unauthorised]

  "the Unauthorised page" must {
    "pass all accessibility tests" in {
      view()(request, messages, config).toString must passAccessibilityChecks
    }
  }

}
