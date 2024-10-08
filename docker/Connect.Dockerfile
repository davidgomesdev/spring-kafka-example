FROM confluentinc/cp-server-connect-base:7.7.1

RUN confluent-hub install --no-prompt debezium/debezium-connector-sqlserver:latest && \
      confluent-hub install --no-prompt confluentinc/kafka-connect-prometheus-metrics:latest
