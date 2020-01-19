package com.moppletop.ddd.autoconfigure.kafka;

import com.moppletop.ddd.autoconfigure.DDDAutoConfiguration;
import com.moppletop.ddd.autoconfigure.kafka.DDDKafkaAutoConfiguration.DDDKafkaProperties;
import com.moppletop.ddd.event.EventGateway;
import com.moppletop.ddd.event.EventStreamer;
import com.moppletop.ddd.eventstream.kafka.KafkaEventStream;
import com.moppletop.ddd.transformer.ObjectTransformer;
import lombok.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Autoconfigure the required beans for the {@link KafkaEventStream}.
 * To configure the properties of the event streamer from within spring, properties "ddd.kafka.xyz" can be set.,
 * see inner class DDDKafkaProperties
 */
@Configuration
@EnableConfigurationProperties(DDDKafkaProperties.class)
@AutoConfigureAfter(DDDAutoConfiguration.class)
public class DDDKafkaAutoConfiguration {

    @Bean
    @ConditionalOnProperty("ddd.kafka.groupId")
    public EventStreamer kafkaEventStreamer(ObjectTransformer objectTransformer,
                                            EventGateway eventGateway,
                                            DDDKafkaProperties properties
    ) {
        return KafkaEventStream.builder()
                .groupId(properties.getGroupId())
                .topic(properties.getTopic())
                .kafkaHosts(properties.getKafkaHosts())
                .pollingRate(Duration.ofMillis(properties.getPollingRate()))
                .objectTransformer(objectTransformer)
                .eventGateway(eventGateway)
                .build();
    }

    @ConfigurationProperties("ddd.kafka")
    @NoArgsConstructor
    @Getter
    @Setter
    public static class DDDKafkaProperties {

        String groupId;
        String topic;
        List<String> kafkaHosts;
        int pollingRate = 1000;

    }
}
