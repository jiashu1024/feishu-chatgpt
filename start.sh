#!/bin/bash

##停止并删除现有的feishubot容器

if docker ps -a --format '{{.Names}}' | grep -q "^feishubot$"; then
    docker stop feishubot
    docker rm feishubot
fi

##拉取最新的feishubot镜像

docker pull zhangjiashu/feishubot

mv accounts-sample.yaml accounts.yaml
mv application-sample.yaml application.yaml

##启动新的feishubot容器

docker run -d \
--name feishubot \
-p 9001:9001 \
-v ./accounts.yaml:/app/accounts.yaml \
-v ./application.yaml:/app/application.yaml zhangjiashu/feishubot