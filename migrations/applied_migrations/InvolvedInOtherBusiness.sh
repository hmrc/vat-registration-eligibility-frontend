#!/bin/bash

echo "Applying migration InvolvedInOtherBusiness"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /involved-more-business-changing-status                                   controllers.InvolvedInOtherBusinessController.onPageLoad()" >> ../conf/app.routes
echo "POST       /involved-more-business-changing-status                                   controllers.InvolvedInOtherBusinessController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "involvedInOtherBusiness.title = Have you been involved with another business or taken over a VAT-registered business?" >> ../conf/messages.en
echo "involvedInOtherBusiness.heading = Have you been involved with another business or taken over a VAT-registered business?" >> ../conf/messages.en
echo "involvedInOtherBusiness.checkYourAnswersLabel = involvedInOtherBusiness" >> ../conf/messages.en
echo "involvedInOtherBusiness.error.required = Please give an answer for involvedInOtherBusiness" >> ../conf/messages.en
echo "involvedInOtherBusiness.text = Tell us if any of the following apply:" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def involvedInOtherBusiness: Option[Boolean] = cacheMap.getEntry[Boolean](InvolvedInOtherBusinessId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def involvedInOtherBusiness: Option[AnswerRow] = userAnswers.involvedInOtherBusiness map {";\
     print "    x => AnswerRow(\"involvedInOtherBusiness.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.InvolvedInOtherBusinessController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration InvolvedInOtherBusiness completed"
