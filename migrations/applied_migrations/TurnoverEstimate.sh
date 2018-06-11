#!/bin/bash

echo "Applying migration TurnoverEstimate"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /turnoverEstimate               controllers.TurnoverEstimateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /turnoverEstimate               controllers.TurnoverEstimateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeTurnoverEstimate               controllers.TurnoverEstimateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeTurnoverEstimate               controllers.TurnoverEstimateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "turnoverEstimate.title = Estimate VAT-taxable turnover for the next 12 months" >> ../conf/messages.en
echo "turnoverEstimate.heading = Estimate VAT-taxable turnover for the next 12 months" >> ../conf/messages.en
echo "turnoverEstimate.zeropounds = £0" >> ../conf/messages.en
echo "turnoverEstimate.oneandtenthousand = �Between £1 and £10000" >> ../conf/messages.en
echo "turnoverEstimate.checkYourAnswersLabel = Estimate VAT-taxable turnover for the next 12 months" >> ../conf/messages.en
echo "turnoverEstimate.error.required = Please give an answer for turnoverEstimate" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def turnoverEstimate: Option[TurnoverEstimate] = cacheMap.getEntry[TurnoverEstimate](TurnoverEstimateId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def turnoverEstimate: Option[AnswerRow] = userAnswers.turnoverEstimate map {";\
     print "    x => AnswerRow(\"turnoverEstimate.checkYourAnswersLabel\", s\"turnoverEstimate.$x\", true, routes.TurnoverEstimateController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration TurnoverEstimate completed"
