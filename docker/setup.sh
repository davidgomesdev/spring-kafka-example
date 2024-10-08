#!/bin/bash

set -e

docker build -t learning-connect -f Connect.Dockerfile .

docker compose up -d

echo -e "\n\n=============\nWaiting for Kafka Connect to start listening on localhost ⏳\n=============\n"

while [ "$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/connectors)" -ne 200 ] ; do
  echo -e "\t" "$(date)" " Kafka Connect listener HTTP state: " "$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/connectors)" " (waiting for 200)"
  sleep 5
done

echo -e "$(date)" "\n\n--------------\n\o/ Kafka Connect is ready! Listener HTTP state: " "$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/connectors)" "\n--------------\n"
