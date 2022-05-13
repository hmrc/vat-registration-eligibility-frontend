
package pages

import models.BusinessEntity._

import forms.BusinessEntityPartnershipFormProvider
import helpers.A11ySpec
import views.html.BusinessEntityPartnership

class BusinessEntityPartnershipA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessEntityPartnership]
  val form = app.injector.instanceOf[BusinessEntityPartnershipFormProvider]

  "the business entity partnership page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form(), testCall)(request, messages, config).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing business entity" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("value" -> "")), testCall)(request, messages, config).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for invalid business entity" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("value" -> `otherKey`)), testCall)(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}
