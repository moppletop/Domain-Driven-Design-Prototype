FROM openjdk:8-alpine

ADD target/ddd-example-driver-*-exec.jar /app/application.jar

ENTRYPOINT exec java -jar /app/application.jar
