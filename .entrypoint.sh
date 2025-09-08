#! /usr/bin/env bash

java -jar -Dliquibase.headless=true \
           -XX:MaxRAM=70m \
           migration.jar \
           --driver=org.postgresql.Driver \
           --url=$JDBC_DATABASE_URL \
           --username=$JDBC_DATABASE_USERNAME \
           --password=$JDBC_DATABASE_PASSWORD \
           --changeLogFile=changelog.xml \
           update

java -jar $@
