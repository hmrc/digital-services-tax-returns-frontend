#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$                  controllers.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /$className;format="decap"$                  controllers.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /change$className$                        controllers.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /change$className$                        controllers.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Enter your $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.invalid = Enter your $className;format="decap"$ using numbers" >> ../conf/messages.en
echo "$className;format="decap"$.error.exceeded = $className$ must be {0} characters or fewer" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
