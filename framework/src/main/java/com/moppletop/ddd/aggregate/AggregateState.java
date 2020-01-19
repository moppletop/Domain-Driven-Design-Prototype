package com.moppletop.ddd.aggregate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Overarching transaction for application of aggregate events
 */
public class AggregateState {

    private static final ThreadLocal<AggregateState> CURRENT_TRANSACTION = ThreadLocal.withInitial(AggregateState::new);

    /**
     * @param aggregateId The aggregate identifier, you're transacting against
     * @return The new instance of AggregateState
     */
    public static AggregateState startTransaction(UUID aggregateId) {
        AggregateState transaction = CURRENT_TRANSACTION.get();

        if (transaction.active) {
            throw new IllegalStateException("Transaction already active");
        }

        transaction.aggregateId = aggregateId;
        transaction.eventBuffer = new LinkedList<>();
        transaction.active = true;
        return transaction;
    }

    /**
     * Stops the transaction, preventing any more events being applied
     * @return A list of events that were applied during the transaction
     */
    public static List<Object> stopTransaction() {
        AggregateState transaction = CURRENT_TRANSACTION.get();

        if (!transaction.active) {
            throw new IllegalStateException("Transaction not active");
        }

        transaction.active = false;
        return transaction.eventBuffer;
    }

    /**
     * Applies some events against the current aggregate.
     * Events must be applied on the same thread that called the {@link com.moppletop.ddd.command.CommandHandler}
     *
     * @param events
     */
    public static void apply(Object... events) {
        AggregateState transaction = CURRENT_TRANSACTION.get();

        if (!transaction.active) {
            throw new IllegalStateException("Transaction not active");
        }

        transaction.eventBuffer.addAll(Arrays.asList(events));
    }

    private UUID aggregateId;
    private List<Object> eventBuffer;
    private boolean active;

}
