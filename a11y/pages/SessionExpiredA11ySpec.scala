package pages

import helpers.A11ySpec
import views.html.SessionExpired

class SessionExpiredA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[SessionExpired]

  "the Session Expired page" must {
    "pass all accessibility tests" in {
      view()(request, messages, config).toString must passAccessibilityChecks
    }
  }

}
