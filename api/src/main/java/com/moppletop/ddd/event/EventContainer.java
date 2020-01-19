package com.moppletop.ddd.event;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * Definition of an event when it's persisted to the database or event stream
 *
 * @param <T> The type of the contained event
 */
@Value
public class EventContainer<T> {

    long globalSequence;
    long globalAggregateId;
    String eventName;
    T payload;
    EventMetadata metadata;

    public EventContainer(Long globalSequence, Long globalAggregateId, T payload, EventMetadata metadata) {
        this.globalSequence = globalSequence;
        this.globalAggregateId = globalAggregateId;
        this.eventName = payload.getClass().getName();
        this.payload = payload;
        this.metadata = metadata;
    }

}
