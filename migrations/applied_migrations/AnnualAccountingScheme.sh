#!/bin/bash

echo "Applying migration AnnualAccountingScheme"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /annual-accounting                                   controllers.AnnualAccountingSchemeController.onPageLoad()" >> ../conf/app.routes
echo "POST       /annual-accounting                                   controllers.AnnualAccountingSchemeController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "annualAccountingScheme.title = Is the business applying for the Annual Accounting Scheme?" >> ../conf/messages.en
echo "annualAccountingScheme.heading = Is the business applying for the Annual Accounting Scheme?" >> ../conf/messages.en
echo "annualAccountingScheme.checkYourAnswersLabel = annualAccountingScheme" >> ../conf/messages.en
echo "annualAccountingScheme.error.required = Please give an answer for annualAccountingScheme" >> ../conf/messages.en
echo "annualAccountingScheme.text = This scheme is an option for businesses with an estimated VAT-taxable turnover of £1.35 million or less." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def annualAccountingScheme: Option[Boolean] = cacheMap.getEntry[Boolean](AnnualAccountingSchemeId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def annualAccountingScheme: Option[AnswerRow] = userAnswers.annualAccountingScheme map {";\
     print "    x => AnswerRow(\"annualAccountingScheme.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.AnnualAccountingSchemeController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AnnualAccountingScheme completed"
