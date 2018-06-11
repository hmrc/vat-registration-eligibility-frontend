#!/bin/bash

echo "Applying migration ThresholdPreviousThirtyDays"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /gone-over-threshold-period                                   controllers.ThresholdPreviousThirtyDaysController.onPageLoad()" >> ../conf/app.routes
echo "POST       /gone-over-threshold-period                                   controllers.ThresholdPreviousThirtyDaysController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "thresholdPreviousThirtyDays.title = Over VAT registration threshold in a 30-day period" >> ../conf/messages.en
echo "thresholdPreviousThirtyDays.heading = Over VAT registration threshold in a 30-day period" >> ../conf/messages.en
echo "thresholdPreviousThirtyDays.checkYourAnswersLabel = thresholdPreviousThirtyDays" >> ../conf/messages.en
echo "thresholdPreviousThirtyDays.error.required = Please give an answer for thresholdPreviousThirtyDays" >> ../conf/messages.en
echo "thresholdPreviousThirtyDays.text = Again, we want to know if the business ever expected to go over the threshold - even if it didn't." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def thresholdPreviousThirtyDays: Option[Boolean] = cacheMap.getEntry[Boolean](ThresholdPreviousThirtyDaysId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def thresholdPreviousThirtyDays: Option[AnswerRow] = userAnswers.thresholdPreviousThirtyDays map {";\
     print "    x => AnswerRow(\"thresholdPreviousThirtyDays.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ThresholdPreviousThirtyDaysController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ThresholdPreviousThirtyDays completed"
