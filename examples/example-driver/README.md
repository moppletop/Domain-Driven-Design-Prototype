# Driver Domain Example

This module is an **extremely** simple domain built on the DDD prototype framework.
It has 3 endpoints. There is an `OpenApi 3` spec for the REST API in this directory.

## Try it out
### Prerequisites
+ Docker

### Get it running
From this directory:
+ `mvn clean install` Builds the framework and driver domain
+ `cd local-test`
+ `docker-compose build` Builds the custom postgres image and driver domain image
+ `docker-compose up` Starts all the containers

Now head to http://localhost:9090 in your browser of choice and register some drivers!
If the logs of the driver domain app, show `Got an async event: ...` then it's all working.

It may take a few seconds to startup as the driver app has to wait for the connector to fully startup before initialising.
This isn't a requirement but because the image attempts to setup the connector between Postgres and Kafka on startup.
See `image-scripts/entrypoint.bash`

### Debugging

To test if the events are getting into Kafka, run this:
```shell script
docker run -it --name watcher --rm --network local-test_default -e ZOOKEEPER_CONNECT=zookeeper -e KAFKA_BROKER=kafka:9092 --link zookeeper:zookeeper --link kafka:kafka debezium/kafka watch-topic -a -k dbserver1.public.event
```
You should see large amounts of `JSON` coming through, if not check that events are getting into the `event` table in
Postgres.

## Flows
### Register Driver
#### Endpoint
`/v1/command/driver/register`
```json
{
  "name": "Sam Lloyd",
  "dateOfBirth": "1999-05-20"
}
```
#### Domain Flow
+ RegisterDriver (Command)
+ DriverAggregate (Aggregate)
+ DriverRegistered (Event)

### Amend Driver
#### Endpoint
`/v1/command/driver/amend`
```json
{
  "driverId": "some-uuid",
  "name": "Sam Lloyd"
}
```
#### Domain Flow
+ AmendDriver (Command)
+ DriverAggregate (Aggregate)
+ DriverAmended (Event)

### Query Driver
#### Endpoint
`/v1/query/driver/get/{driverId}`
#### Domain Flow
+ driver_by_id (Query)
+ DriverRepository