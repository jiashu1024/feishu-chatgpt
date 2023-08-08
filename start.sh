#!/bin/bash
for pid in $(ps -ef | grep 'FeiShuBot' | awk '{print $2}'); do
  kill -9 $pid
done

mvn clean package -DskipTests

cp target/*.jar .

jar_file=$(ls *.jar)

mv accounts-sample.yaml accounts.yaml
mv application-sample.yaml application.yaml

nohup java -jar $jar_file > bot.log 2>&1 &