#!/bin/bash

echo "Applying migration ZeroRatedSales"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /zero-rated-sales                                   controllers.ZeroRatedSalesController.onPageLoad()" >> ../conf/app.routes
echo "POST       /zero-rated-sales                                   controllers.ZeroRatedSalesController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "zeroRatedSales.title = Does the business sell mainly zero-rated goods or services?" >> ../conf/messages.en
echo "zeroRatedSales.heading = Does the business sell mainly zero-rated goods or services?" >> ../conf/messages.en
echo "zeroRatedSales.checkYourAnswersLabel = zeroRatedSales" >> ../conf/messages.en
echo "zeroRatedSales.error.required = Please give an answer for zeroRatedSales" >> ../conf/messages.en
echo "zeroRatedSales.text = Examples of zero-rated goods or services" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def zeroRatedSales: Option[Boolean] = cacheMap.getEntry[Boolean](ZeroRatedSalesId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def zeroRatedSales: Option[AnswerRow] = userAnswers.zeroRatedSales map {";\
     print "    x => AnswerRow(\"zeroRatedSales.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ZeroRatedSalesController.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ZeroRatedSales completed"
