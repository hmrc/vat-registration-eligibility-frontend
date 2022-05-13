
package pages

import forms.InvolvedInOtherBusinessFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.InvolvedInOtherBusiness

class iInvolvedInOtherBusinessA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[InvolvedInOtherBusiness]
  val form = app.injector.instanceOf[InvolvedInOtherBusinessFormProvider]

  "the involved in other business page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form.form, NormalMode)(request, messages, config).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing form value" must {
      "pass all accessibility tests" in {
        view(form.form.bind(Map("value" -> "")), NormalMode)(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}