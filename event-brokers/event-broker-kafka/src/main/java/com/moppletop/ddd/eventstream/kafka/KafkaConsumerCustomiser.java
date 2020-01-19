package com.moppletop.ddd.eventstream.kafka;

import java.util.Properties;

/**
 * Use this to customer the properties of a kakfa consumer, these can be registered either in the builder of {@link KafkaEventStream}
 * or {@link KafkaEventStream#addPropertiesCustomiser(KafkaConsumerCustomiser)}
 * <a href="https://kafka.apache.org/documentation/#topicconfigs">Kafka Documentation</a>
 */
@FunctionalInterface
public interface KafkaConsumerCustomiser {

    void customise(Properties properties, String processingGroup, int threadId);

}
