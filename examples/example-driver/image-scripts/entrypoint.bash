#!/bin/bash

while true; do
  response=$(curl -s -X POST -H "Content-Type:application/json" connect:8083/connectors/ -d '{ "name": "driver_app","config": {"connector.class": "io.debezium.connector.postgresql.PostgresConnector","tasks.max": "1","database.hostname": "postgres","database.port": "5432","database.user": "postgres","database.password": "password","database.dbname": "driver_db","database.server.name": "dbserver1", "database.whitelist": "driver_db", "database.history.kafka.bootstrap.servers": "kafka:9092", "database.history.kafka.topic": "schema-changes"}}')

  echo "$response"

  if [[ $response == \{* ]]; then
    break
  fi

  sleep 3
done

exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=2 -jar /app/application.jar
