#!/bin/bash

echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$url$                                   controllers.$className$Controller.onPageLoad()" >> ../conf/app.routes
echo "POST       /$url$                                   controllers.$className$Controller.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Please give an answer for $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.text = $text$" >> ../conf/messages.en

if [ ! -z "$yesHiddenContent$" ]
then
    echo "$className;format="decap"$.hiddenContent.yes = $yesHiddenContent$" >> ../conf/messages.en
fi

if [ ! -z "$noHiddenContent$" ]
then
    echo "$className;format="decap"$.hiddenContent.no = $noHiddenContent$" >> ../conf/messages.en
fi

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def $className;format="decap"$: Option[Boolean] = cacheMap.getEntry[Boolean]($className$Id.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def $className;format="decap"$: Option[AnswerRow] = userAnswers.$className;format="decap"$ map {";\
     print "    x => AnswerRow(\"$className;format="decap"$.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.$className$Controller.onPageLoad().url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration $className;format="snake"$ completed"
