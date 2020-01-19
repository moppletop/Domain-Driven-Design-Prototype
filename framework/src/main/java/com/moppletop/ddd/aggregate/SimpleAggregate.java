package com.moppletop.ddd.aggregate;

import lombok.*;

import java.util.UUID;

@Data
@Builder
public class SimpleAggregate<T> implements Aggregate<T> {

    @EqualsAndHashCode.Include
    private final long globalId;

    private final UUID aggregateIdentifier;
    private final String className;
    private final T currentState;
    private final long initialEventSequence;

    private long eventSequence;

}
