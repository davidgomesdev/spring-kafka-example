#!/usr/bin/env bash

printf "Launching compose\n\n"

docker compose up

# Wait for Debezium to be up
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8083)" != "200" ]]; do
  echo "Waiting for Debezium..."
  sleep 5
done

printf "Adding connector config\n\n"

curl -i -X POST -H "Accept:application/json" -H "Content-Type: application/json" \
  http://localhost:8083/connectors -d @config/pg-connector.json
