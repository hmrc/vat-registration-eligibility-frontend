#!/bin/bash

echo "Applying migration ApplicationUKNino"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /national-insurance-number                                   controllers.ApplicationUKNinoController.onPageLoad()" >> ../conf/app.routes
echo "POST       /national-insurance-number                                   controllers.ApplicationUKNinoController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "applicationUKNino.title = Do you have a UK National Insurance number?" >> ../conf/messages.en
echo "applicationUKNino.heading = Do you have a UK National Insurance number?" >> ../conf/messages.en
echo "applicationUKNino.checkYourAnswersLabel = applicationUKNino" >> ../conf/messages.en
echo "applicationUKNino.error.required = Please give an answer for applicationUKNino" >> ../conf/messages.en
echo "applicationUKNino.text = Some text to provide more info." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def applicationUKNino: Option[Boolean] = cacheMap.getEntry[Boolean](ApplicationUKNinoId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def applicationUKNino: Option[AnswerRow] = userAnswers.applicationUKNino map {";\
     print "    x => AnswerRow(\"applicationUKNino.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ApplicationUKNinoController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ApplicationUKNino completed"
