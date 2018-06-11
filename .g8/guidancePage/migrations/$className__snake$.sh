#!/bin/bash

echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /$url$                       controllers.$className$Controller.onPageLoad()" >> ../conf/app.routes
echo "POST       /$url$                       controllers.$className$Controller.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $title$" >> ../conf/messages.en
echo "$className;format="decap"$.text = $text$" >> ../conf/messages.en
echo "$className;format="decap"$.bullet1 = $bullet1$" >> ../conf/messages.en
echo "$className;format="decap"$.bullet2 = $bullet2$" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration $className;format="snake"$ completed"
