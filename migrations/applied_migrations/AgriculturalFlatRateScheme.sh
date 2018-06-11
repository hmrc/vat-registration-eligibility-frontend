#!/bin/bash

echo "Applying migration AgriculturalFlatRateScheme"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /agricultural-flat-rate                                   controllers.AgriculturalFlatRateSchemeController.onPageLoad()" >> ../conf/app.routes
echo "POST       /agricultural-flat-rate                                   controllers.AgriculturalFlatRateSchemeController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "agriculturalFlatRateScheme.title = Is the business applying for the Agricultural Flat Rate Scheme?" >> ../conf/messages.en
echo "agriculturalFlatRateScheme.heading = Is the business applying for the Agricultural Flat Rate Scheme?" >> ../conf/messages.en
echo "agriculturalFlatRateScheme.checkYourAnswersLabel = agriculturalFlatRateScheme" >> ../conf/messages.en
echo "agriculturalFlatRateScheme.error.required = Please give an answer for agriculturalFlatRateScheme" >> ../conf/messages.en
echo "agriculturalFlatRateScheme.text = The scheme is a different type of VAT registration for farmers." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def agriculturalFlatRateScheme: Option[Boolean] = cacheMap.getEntry[Boolean](AgriculturalFlatRateSchemeId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def agriculturalFlatRateScheme: Option[AnswerRow] = userAnswers.agriculturalFlatRateScheme map {";\
     print "    x => AnswerRow(\"agriculturalFlatRateScheme.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.AgriculturalFlatRateSchemeController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AgriculturalFlatRateScheme completed"
