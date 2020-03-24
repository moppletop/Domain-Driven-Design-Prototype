package com.moppletop.ddd.eventstream.kafka;

import com.moppletop.ddd.event.EventContainer;
import com.moppletop.ddd.event.EventGateway;
import com.moppletop.ddd.event.EventMetadata;
import com.moppletop.ddd.event.EventStreamer;
import com.moppletop.ddd.transformer.ObjectTransformer;
import com.moppletop.ddd.util.EventStreamThreadFactory;
import lombok.Builder;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * The <a href="https://kafka.apache.org/documentation">Kafka</a> implementation of the event streamer
 * This implementation has one thread per partition for simplicity of implementation.
 * There are a list of pros and cons of this implementation <a href="https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html">here</a>
 */
@Slf4j
public class KafkaEventStream implements EventStreamer {

    private static final String SERIALISER = "org.apache.kafka.common.serialization.StringDeserializer";

    private final String groupId;
    private final String topic;
    private final Collection<String> kafkaHosts;
    private final Duration pollingRate;

    private final List<KafkaConsumerCustomiser> customisers;

    private final ObjectTransformer objectTransformer;
    private final EventGateway eventGateway;

    private final AtomicBoolean running;
    private final List<KafkaConsumer<String, String>> consumers;

    @Builder
    public KafkaEventStream(String groupId,
                            String topic,
                            @Singular Collection<String> kafkaHosts,
                            Duration pollingRate,
                            @Singular List<KafkaConsumerCustomiser> customisers,
                            ObjectTransformer objectTransformer,
                            EventGateway eventGateway
    ) {
        this.groupId = groupId;
        this.topic = topic;
        this.kafkaHosts = kafkaHosts;
        this.pollingRate = pollingRate;
        this.customisers = customisers == null ? new ArrayList<>() : new ArrayList<>(customisers);
        this.objectTransformer = objectTransformer;
        this.eventGateway = eventGateway;

        this.running = new AtomicBoolean(true);
        this.consumers = Collections.synchronizedList(new ArrayList<>());

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    @Override
    public void shutdown() {
        log.info("Shutting down Kafka event streamer {}...", groupId);
        running.set(false);
        consumers.forEach(KafkaConsumer::wakeup);
    }

    public void addPropertiesCustomiser(KafkaConsumerCustomiser customiser) {
        Objects.requireNonNull(customiser, "Cannot register a null customiser");
        customisers.add(customiser);
    }

    @Override
    public void subscribe(String processingGroup, int threads) {
        log.debug("Subscribing with {} thread{} to processing group {}", threads, threads == 1 ? "" : "s", processingGroup);
        ThreadFactory threadFactory = new EventStreamThreadFactory(processingGroup);

        for (int i = 1; i <= threads; i++) {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(buildProperties(processingGroup, i));
            consumers.add(consumer);

            // Start a new thread and begin polling
            threadFactory.newThread(() -> {
                try {
                    poll(consumer);
                } finally {
                    consumer.close();
                }
            }).start();
        }
    }

    private void poll(KafkaConsumer<String, String> consumer) {
        // Map the partition id to the current offset
        Map<Integer, Long> offsets = new HashMap<>();
        /*
            TODO something to think about?
            The documentation is slightly vague about this, but since we're only every committing to Kafka, only use one
            thread and never save the offsets ourselves there should be no reason to implement a
            ConsumerRebalanceListener and commit the current offset, since the listener is invoked as part of
            the poll(timeout) call.
            See https://kafka.apache.org/10/javadoc/org/apache/kafka/clients/consumer/ConsumerRebalanceListener.html
         */
        consumer.subscribe(Collections.singletonList(topic));

        pollLoop:
        while (running.get()) {
            ConsumerRecords<String, String> records;

            try {
                records = consumer.poll(pollingRate);
            } catch (WakeupException ex) {
                continue;
            }

            for (ConsumerRecord<String, String> record : records) {
                try {
                    processRecord(record);
                    offsets.put(record.partition(), record.offset() + 1);
                } catch (Exception ex) {
                    log.error("A stream event handler threw an unhandled exception, committing the current offset, backing off for 1 second and attempt to reprocess...", ex);

                    Map<TopicPartition, OffsetAndMetadata> committed = offsets.entrySet().stream()
                            // We subtract 1 here since we want to retry the event that just threw the exception, not continue from the next event
                            .map(entry -> new SimpleEntry<>(new TopicPartition(topic, entry.getKey()), new OffsetAndMetadata(entry.getValue() - 1)))
                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

                    // Commit what we've processed and clear all offsets
                    consumer.commitSync(committed);
                    offsets.clear();

                    // Back off for one second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }

                    continue pollLoop;
                }
            }

            // Commit everything and clear all offsets
            consumer.commitSync();
            offsets.clear();
        }
    }

    private void processRecord(ConsumerRecord<String, String> record) throws Exception {
        // TODO sort out this mess
        Map<?, ?> asMap = objectTransformer.deserialise(record.value(), Map.class);
        asMap = (Map<?, ?>) asMap.get("payload");
        asMap = (Map<?, ?>) asMap.get("after");

        // Deserialise the payload and wrap it in an EventContainer
        String className = (String) asMap.get("class_name");
        Class<?> classOfEvent = Class.forName(className);

        EventContainer<?> eventContainer = new EventContainer<>(
                Long.parseLong(String.valueOf(asMap.get("global_sequence"))),
                Long.parseLong(String.valueOf(asMap.get("aggregate_global_id"))),
                objectTransformer.deserialise((String) asMap.get("payload"), classOfEvent),
                objectTransformer.deserialise((String) asMap.get("metadata"), EventMetadata.class)
        );

        // Notify the gateway we have a new event, this will call all of the handlers
        eventGateway.handleStreamedEvent(eventContainer);
    }

    // Default properties of the KafkaConsumer
    // By default we use the hostname of localhost as part of the client id, which is unlikely to actually throw a
    // UnknownHostException, so we sneakily throw it
    @SneakyThrows(UnknownHostException.class)
    private Properties buildProperties(String processingGroup, int id) {
        Properties properties = new Properties();

        properties.put("group.id", groupId + '.' + processingGroup);
        properties.put("client.id", groupId + '.' + processingGroup + '.' + InetAddress.getLocalHost().getHostName() + '.' + id);
        properties.put("bootstrap.servers", String.join(",", kafkaHosts));
        properties.put("key.deserializer", SERIALISER);
        properties.put("value.deserializer", SERIALISER);
        properties.put("session.timeout.ms", 30000);
        properties.put("max.poll.records", 50); // Only allow up to 50 events to be processed in one polling cycle
        properties.put("fetch.min.bytes", 1024); // Minimum about of bytes required for a events to be published
        properties.put("fetch.max.wait.ms", 1000); // Maximum ms the consumer will wait with a event buffer of bytes (0 < size < 1024)

        customisers.forEach(customiser -> customiser.customise(properties, processingGroup, id));

        log.debug("Properties processing group: {}", properties);

        return properties;
    }
}
