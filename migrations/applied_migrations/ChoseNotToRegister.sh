#!/bin/bash

echo "Applying migration ChoseNotToRegister"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /choseNotToRegister                       controllers.ChoseNotToRegisterController.onPageLoad()" >> ../conf/app.routes
echo "POST       /choseNotToRegister                       controllers.ChoseNotToRegisterController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "choseNotToRegister.title = You have chosen not to register the business for VAT" >> ../conf/messages.en
echo "choseNotToRegister.heading = You have chosen not to register the business for VAT" >> ../conf/messages.en
echo "choseNotToRegister.text = Enter some guidance text here " >> ../conf/messages.en
echo "choseNotToRegister.bullet1 = guidance bullet 1" >> ../conf/messages.en
echo "choseNotToRegister.bullet2 = guidance bullet 2" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ChoseNotToRegister completed"
