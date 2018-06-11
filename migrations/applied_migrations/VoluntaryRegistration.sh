#!/bin/bash

echo "Applying migration VoluntaryRegistration"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /register-voluntarily                                   controllers.VoluntaryRegistrationController.onPageLoad()" >> ../conf/app.routes
echo "POST       /register-voluntarily                                   controllers.VoluntaryRegistrationController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "voluntaryRegistration.title = Do you want to register the business voluntarily?" >> ../conf/messages.en
echo "voluntaryRegistration.heading = Do you want to register the business voluntarily?" >> ../conf/messages.en
echo "voluntaryRegistration.checkYourAnswersLabel = voluntaryRegistration" >> ../conf/messages.en
echo "voluntaryRegistration.error.required = Please give an answer for voluntaryRegistration" >> ../conf/messages.en
echo "voluntaryRegistration.text = You can still register the business voluntarily, if it:" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def voluntaryRegistration: Option[Boolean] = cacheMap.getEntry[Boolean](VoluntaryRegistrationId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def voluntaryRegistration: Option[AnswerRow] = userAnswers.voluntaryRegistration map {";\
     print "    x => AnswerRow(\"voluntaryRegistration.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.VoluntaryRegistrationController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration VoluntaryRegistration completed"
