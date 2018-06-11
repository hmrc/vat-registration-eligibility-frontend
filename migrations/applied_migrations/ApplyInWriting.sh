#!/bin/bash

echo "Applying migration ApplyInWriting"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /applyInWriting                       controllers.ApplyInWritingController.onPageLoad()" >> ../conf/app.routes
echo "POST       /applyInWriting                       controllers.ApplyInWritingController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "applyInWriting.title = You must apply in writing" >> ../conf/messages.en
echo "applyInWriting.heading = You must apply in writing" >> ../conf/messages.en
echo "applyInWriting.text = Please use form VAT1 to apply for an exemption from VAT registration." >> ../conf/messages.en
echo "applyInWriting.bullet1 = guidance bullet 1" >> ../conf/messages.en
echo "applyInWriting.bullet2 = guidance bullet 2" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ApplyInWriting completed"
