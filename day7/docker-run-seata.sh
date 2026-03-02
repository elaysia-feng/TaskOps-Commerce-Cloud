#!/usr/bin/env bash

# 在 seata 目录执行前，确保当前目录下存在 ./seata-resources/application.yml
# 例如：cd ~/springCloud/seata

docker run --name seata \
  -p 7099:7099 \
  -p 8099:8099 \
  -e SEATA_IP=192.168.88.100 \
  -e SEATA_SERVER_HTTP_PORT=7099 \
  -e SEATA_SERVER_SERVICE_PORT=8099 \
  -e TASKOPS_MW_HOST=192.168.88.100 \
  -e TASKOPS_DB_USER=root \
  -e TASKOPS_DB_PASS=root \
  -e NACOS_USERNAME=nacos \
  -e NACOS_PASSWORD=nacos \
  -v ./seata-resources:/seata-server/resources \
  --privileged=true \
  --network springcloud_default \
  --network-alias seata-server \
  --restart=always \
  -d seataio/seata-server:1.5.2
