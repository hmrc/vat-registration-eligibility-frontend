#!/bin/bash

echo "Applying migration CompletionCapacityFillingInFor"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /completionCapacityFillingInFor               controllers.CompletionCapacityFillingInForController.onPageLoad()" >> ../conf/app.routes
echo "POST       /completionCapacityFillingInFor               controllers.CompletionCapacityFillingInForController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "completionCapacityFillingInFor.title = Which company officer are you doing this application for?" >> ../conf/messages.en
echo "completionCapacityFillingInFor.heading = Which company officer are you doing this application for?" >> ../conf/messages.en
echo "completionCapacityFillingInFor.companyOfficer 1 = CompanyOfficer 2" >> ../conf/messages.en
echo "completionCapacityFillingInFor.option2 = Option 2" >> ../conf/messages.en
echo "completionCapacityFillingInFor.checkYourAnswersLabel = Which company officer are you doing this application for?" >> ../conf/messages.en
echo "completionCapacityFillingInFor.error.required = Please give an answer for completionCapacityFillingInFor" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def completionCapacityFillingInFor: Option[CompletionCapacityFillingInFor] = cacheMap.getEntry[CompletionCapacityFillingInFor](CompletionCapacityFillingInForId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def completionCapacityFillingInFor: Option[AnswerRow] = userAnswers.completionCapacityFillingInFor map {";\
     print "    x => AnswerRow(\"completionCapacityFillingInFor.checkYourAnswersLabel\", s\"completionCapacityFillingInFor.$x\", true, routes.CompletionCapacityFillingInForController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompletionCapacityFillingInFor completed"
