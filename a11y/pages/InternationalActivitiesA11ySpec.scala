
package pages

import forms.InternationalActivitiesFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.InternationalActivities

class InternationalActivitiesA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[InternationalActivities]
  val form = app.injector.instanceOf[InternationalActivitiesFormProvider]

  "the international activities page" when {
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