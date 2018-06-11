#!/bin/bash

echo "Applying migration VATExemption"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /vat-exemption                                   controllers.VATExemptionController.onPageLoad()" >> ../conf/app.routes
echo "POST       /vat-exemption                                   controllers.VATExemptionController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vATExemption.title = Do you want to apply for a VAT exemption?" >> ../conf/messages.en
echo "vATExemption.heading = Do you want to apply for a VAT exemption?" >> ../conf/messages.en
echo "vATExemption.checkYourAnswersLabel = vATExemption" >> ../conf/messages.en
echo "vATExemption.error.required = Please give an answer for vATExemption" >> ../conf/messages.en
echo "vATExemption.text = The business may not have to register for VAT if it sells mainly or only zero-rated goods or services." >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def vATExemption: Option[Boolean] = cacheMap.getEntry[Boolean](VATExemptionId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def vATExemption: Option[AnswerRow] = userAnswers.vATExemption map {";\
     print "    x => AnswerRow(\"vATExemption.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.VATExemptionController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration VATExemption completed"
