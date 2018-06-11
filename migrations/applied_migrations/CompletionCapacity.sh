#!/bin/bash

echo "Applying migration CompletionCapacity"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /completionCapacity               controllers.CompletionCapacityController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /completionCapacity               controllers.CompletionCapacityController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCompletionCapacity               controllers.CompletionCapacityController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCompletionCapacity               controllers.CompletionCapacityController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "completionCapacity.title = Which of the following people are you?" >> ../conf/messages.en
echo "completionCapacity.heading = Which of the following people are you?" >> ../conf/messages.en
echo "completionCapacity.herNameWasLola = Lola Mariboro" >> ../conf/messages.en
echo "completionCapacity.noneOfThese = None of these" >> ../conf/messages.en
echo "completionCapacity.checkYourAnswersLabel = Which of the following people are you?" >> ../conf/messages.en
echo "completionCapacity.error.required = Please give an answer for completionCapacity" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def completionCapacity: Option[CompletionCapacity] = cacheMap.getEntry[CompletionCapacity](CompletionCapacityId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def completionCapacity: Option[AnswerRow] = userAnswers.completionCapacity map {";\
     print "    x => AnswerRow(\"completionCapacity.checkYourAnswersLabel\", s\"completionCapacity.$x\", true, routes.CompletionCapacityController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompletionCapacity completed"
