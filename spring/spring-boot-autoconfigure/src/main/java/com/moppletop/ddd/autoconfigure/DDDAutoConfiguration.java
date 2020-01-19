package com.moppletop.ddd.autoconfigure;

import com.moppletop.ddd.aggregate.AggregateRepository;
import com.moppletop.ddd.aggregate.JdbcAggregateRepository;
import com.moppletop.ddd.command.CommandGateway;
import com.moppletop.ddd.command.DefaultCommandGateway;
import com.moppletop.ddd.dependency.DependencyRegister;
import com.moppletop.ddd.event.DefaultEventGateway;
import com.moppletop.ddd.event.EventGateway;
import com.moppletop.ddd.event.EventRepository;
import com.moppletop.ddd.event.JdbcEventRepository;
import com.moppletop.ddd.query.DefaultQueryGateway;
import com.moppletop.ddd.query.QueryGateway;
import com.moppletop.ddd.spring.SpringDependencyRegister;
import com.moppletop.ddd.spring.SpringWiringManager;
import com.moppletop.ddd.transformer.ObjectTransformer;
import com.moppletop.ddd.transformer.jackson.JacksonObjectTransformer;
import com.moppletop.ddd.wiring.WiringManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Autoconfigure all default beans to enable to DDD framework to function
 */
@Configuration
public class DDDAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DependencyRegister dependencyRegistrar(ApplicationContext context) {
        return new SpringDependencyRegister(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectTransformer objectTransformer() {
        return new JacksonObjectTransformer();
    }

    @Bean
    @ConditionalOnMissingBean
    public AggregateRepository aggregateRepository(ObjectTransformer objectTransformer) {
        return new JdbcAggregateRepository(objectTransformer);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventRepository eventRepository(ObjectTransformer objectTransformer) {
        return new JdbcEventRepository(objectTransformer);
    }

    @Bean
    @ConditionalOnMissingBean
    public WiringManager wiringManager(DependencyRegister dependencyRegister) {
        return new SpringWiringManager(dependencyRegister);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public CommandGateway commandGateway(DataSource dataSource,
                                         AggregateRepository aggregateRepository,
                                         EventRepository eventRepository,
                                         WiringManager wiringManager
    ) {
        return new DefaultCommandGateway(dataSource, aggregateRepository, eventRepository, wiringManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventGateway eventGateway(WiringManager wiringManager) {
        return new DefaultEventGateway(wiringManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryGateway queryGateway(WiringManager wiringManager) {
        return new DefaultQueryGateway(wiringManager);
    }

}
