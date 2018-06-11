#!/bin/bash

echo "Applying migration Racehorses"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /own-racehorses-buy-sell-property                                   controllers.RacehorsesController.onPageLoad()" >> ../conf/app.routes
echo "POST       /own-racehorses-buy-sell-property                                   controllers.RacehorsesController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "racehorses.title = Will the business be doing any of the following?" >> ../conf/messages.en
echo "racehorses.heading = Will the business be doing any of the following?" >> ../conf/messages.en
echo "racehorses.checkYourAnswersLabel = racehorses" >> ../conf/messages.en
echo "racehorses.error.required = Please give an answer for racehorses" >> ../conf/messages.en
echo "racehorses.text = Tell us if the business will:" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def racehorses: Option[Boolean] = cacheMap.getEntry[Boolean](RacehorsesId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def racehorses: Option[AnswerRow] = userAnswers.racehorses map {";\
     print "    x => AnswerRow(\"racehorses.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.RacehorsesController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Racehorses completed"
