#!/bin/bash

echo "Applying migration InternationalActivities"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /international-business                                   controllers.InternationalActivitiesController.onPageLoad()" >> ../conf/app.routes
echo "POST       /international-business                                   controllers.InternationalActivitiesController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "internationalActivities.title = Will the business be doing any of the following international activities over the next 12 months?" >> ../conf/messages.en
echo "internationalActivities.heading = Will the business be doing any of the following international activities over the next 12 months?" >> ../conf/messages.en
echo "internationalActivities.checkYourAnswersLabel = internationalActivities" >> ../conf/messages.en
echo "internationalActivities.error.required = Please give an answer for internationalActivities" >> ../conf/messages.en
echo "internationalActivities.text = Tell us if the business will:" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def internationalActivities: Option[Boolean] = cacheMap.getEntry[Boolean](InternationalActivitiesId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def internationalActivities: Option[AnswerRow] = userAnswers.internationalActivities map {";\
     print "    x => AnswerRow(\"internationalActivities.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.InternationalActivitiesController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration InternationalActivities completed"
