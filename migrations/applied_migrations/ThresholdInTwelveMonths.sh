#!/bin/bash

echo "Applying migration ThresholdInTwelveMonths"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /gone-over-threshold                                   controllers.ThresholdInTwelveMonthsController.onPageLoad()" >> ../conf/app.routes
echo "POST       /gone-over-threshold                                   controllers.ThresholdInTwelveMonthsController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "thresholdInTwelveMonths.title = Over VAT-registration threshold in a 12-month period" >> ../conf/messages.en
echo "thresholdInTwelveMonths.heading = Over VAT-registration threshold in a 12-month period" >> ../conf/messages.en
echo "thresholdInTwelveMonths.checkYourAnswersLabel = thresholdInTwelveMonths" >> ../conf/messages.en
echo "thresholdInTwelveMonths.error.required = Please give an answer for thresholdInTwelveMonths" >> ../conf/messages.en
echo "thresholdInTwelveMonths.text = We want to know the month that VAT-taxable sales went above the threshold." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def thresholdInTwelveMonths: Option[Boolean] = cacheMap.getEntry[Boolean](ThresholdInTwelveMonthsId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def thresholdInTwelveMonths: Option[AnswerRow] = userAnswers.thresholdInTwelveMonths map {";\
     print "    x => AnswerRow(\"thresholdInTwelveMonths.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ThresholdInTwelveMonthsController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ThresholdInTwelveMonths completed"
