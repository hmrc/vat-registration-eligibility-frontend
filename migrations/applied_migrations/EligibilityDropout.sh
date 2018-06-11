#!/bin/bash

echo "Applying migration EligibilityDropout"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /eligibilityDropout                       controllers.EligibilityDropoutController.onPageLoad()" >> ../conf/app.routes
echo "POST       /eligibilityDropout                       controllers.EligibilityDropoutController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "eligibilityDropout.title = You can’t register for VAT with this service" >> ../conf/messages.en
echo "eligibilityDropout.heading = You can’t register for VAT with this service" >> ../conf/messages.en
echo "eligibilityDropout.text = You'll need to register using another HMRC service." >> ../conf/messages.en
echo "eligibilityDropout.bullet1 = guidance bullet 1" >> ../conf/messages.en
echo "eligibilityDropout.bullet2 = guidance bullet 2" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration EligibilityDropout completed"
