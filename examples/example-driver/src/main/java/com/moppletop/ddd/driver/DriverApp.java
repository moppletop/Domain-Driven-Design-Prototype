package com.moppletop.ddd.driver;

import com.moppletop.ddd.aggregate.AggregateRepository;
import com.moppletop.ddd.aggregate.JdbcAggregateRepository;
import com.moppletop.ddd.command.CommandGateway;
import com.moppletop.ddd.command.DefaultCommandGateway;
import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.dependeny.ExplicitDependencyRegister;
import com.moppletop.ddd.event.*;
import com.moppletop.ddd.eventstream.kafka.KafkaEventStream;
import com.moppletop.ddd.query.DefaultQueryGateway;
import com.moppletop.ddd.query.QueryGateway;
import com.moppletop.ddd.transformer.ObjectTransformer;
import com.moppletop.ddd.transformer.jackson.JacksonObjectTransformer;
import com.moppletop.ddd.wiring.ExplicitWiringManager;
import com.moppletop.ddd.wiring.WiringManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.time.Duration;

@SpringBootApplication
public class DriverApp {

    public static void main(String[] args) {
        SpringApplication.run(DriverApp.class, args);
    }

    private final DependencyRegister dependencyRegister;
    private final ObjectTransformer objectTransformer;
    private final AggregateRepository aggregateRepository;
    private final EventRepository eventRepository;
    private final ExplicitWiringManager wiringManager;
    private final CommandGateway commandGateway;
    private final EventGateway eventGateway;
    private final QueryGateway queryGateway;

    private final EventStreamer eventStreamer;

    public DriverApp(DataSource dataSource) {
        this.dependencyRegister = new ExplicitDependencyRegister();
        // Note this requires Jackson 2 on the classpath!
        // If you are using another serialisation library, implement your own ObjectTransformer
        this.objectTransformer = new JacksonObjectTransformer();
        this.aggregateRepository = new JdbcAggregateRepository(objectTransformer);
        this.eventRepository = new JdbcEventRepository(objectTransformer);
        this.wiringManager = new ExplicitWiringManager(dependencyRegister);
        this.commandGateway = new DefaultCommandGateway(
                dataSource,
                aggregateRepository,
                eventRepository,
                wiringManager
        );
        this.eventGateway = new DefaultEventGateway(wiringManager);
        this.queryGateway = new DefaultQueryGateway(wiringManager);

        this.eventStreamer = KafkaEventStream.builder()
                .groupId("example-app")
                .topic("dbserver1.public.event")
                .kafkaHost("kafka:9092")
                .pollingRate(Duration.ofSeconds(1))
                .objectTransformer(objectTransformer)
                .eventGateway(eventGateway)
                .build();

//        wiringManager.registerAggregate(MyAggregate.class);
//        wiringManager.registerAggregate(MyOtherAggregate.class);
//
//        wiringManager.registerDomainEventHandlers(new MyDomainEventHandlers());
//        wiringManager.re
    }

}
