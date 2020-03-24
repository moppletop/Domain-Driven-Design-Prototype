FROM openjdk:8-alpine

RUN apk update && apk add curl && apk add bash

WORKDIR /app

ADD image-scripts/entrypoint.bash entrypoint.bash
RUN chmod +x entrypoint.bash

ADD target/ddd-example-driver-*-exec.jar application.jar

ENTRYPOINT /app/entrypoint.bash
