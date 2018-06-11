#!/bin/bash

echo "Applying migration ThresholdNextThirtyDays"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /make-more-taxable-sales                                   controllers.ThresholdNextThirtyDaysController.onPageLoad()" >> ../conf/app.routes
echo "POST       /make-more-taxable-sales                                   controllers.ThresholdNextThirtyDaysController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "thresholdNextThirtyDays.title = Over VAT-registration threshold in the next 30 days" >> ../conf/messages.en
echo "thresholdNextThirtyDays.heading = Over VAT-registration threshold in the next 30 days" >> ../conf/messages.en
echo "thresholdNextThirtyDays.checkYourAnswersLabel = thresholdNextThirtyDays" >> ../conf/messages.en
echo "thresholdNextThirtyDays.error.required = Please give an answer for thresholdNextThirtyDays" >> ../conf/messages.en
echo "thresholdNextThirtyDays.text = £[85,000] is the current VAT-registration threshold. It’s the amount of VAT-taxable sales a business can make before it has to register for VAT." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def thresholdNextThirtyDays: Option[Boolean] = cacheMap.getEntry[Boolean](ThresholdNextThirtyDaysId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def thresholdNextThirtyDays: Option[AnswerRow] = userAnswers.thresholdNextThirtyDays map {";\
     print "    x => AnswerRow(\"thresholdNextThirtyDays.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ThresholdNextThirtyDaysController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ThresholdNextThirtyDays completed"
