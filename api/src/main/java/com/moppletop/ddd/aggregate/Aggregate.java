package com.moppletop.ddd.aggregate;

import java.util.UUID;

/**
 * Wrapper for an aggregate, used internally by the framework implementation
 *
 * @param <T> The type of the actual aggregate class
 */
public interface Aggregate<T> {

    /**
     * @return The global aggregate id, used for database mapping and internally by the framework
     */
    long getGlobalId();

    /**
     * @return The business aggregate identifier
     */
    UUID getAggregateIdentifier();

    /**
     * @return The name of type T's class
     */
    String getClassName();

    /**
     * Aggregates need to be aware of the latest event sequence number. This is because when multiple nodes of an app are running
     * it's possible for two or more nodes to apply events at the same time, this would result in the current state of the aggregate
     * being incorrect. For example:
     *
     * <pre>
     *     Node 1 loads state
     *     Node 2 loads state
     *     Node 1 applies an event
     *     Node 2 applies an event
     *     Node 1 saves state
     *     Node 2 saves state
     *
     *     The state now only reflects the event applied by Node 2
     * </pre>
     *
     * The solution to this is to keep track of the last event sequence number that was applied to an aggregate when loading it.
     * After applying some events. Save the aggregate state and check that the last event sequence number has not changed
     * since the aggregate was loaded. If it has fail the transaction and rollback.
     *
     * @return The initial event sequence number of the aggregate when it was loaded
     */
    long getInitialEventSequence();

    /**
     * @see Aggregate#getInitialEventSequence()
     * @return The current event sequence number of the aggregate
     */
    long getEventSequence();

    /**
     * @return The current state of the aggregate, in other words, the actual instance of the aggregate
     */
    T getCurrentState();

    /**
     * @return Whether or not an aggregate exists within the database
     */
    default boolean exists() {
        return getGlobalId() > 0;
    }

}
