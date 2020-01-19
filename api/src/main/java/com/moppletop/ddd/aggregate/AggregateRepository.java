package com.moppletop.ddd.aggregate;

import com.moppletop.ddd.database.ConnectionProvider;

import java.util.UUID;

/**
 * Repository for storing the current state of an aggregate
 */
public interface AggregateRepository {

    /**
     * Loads an aggregates current state
     *
     * @param connectionProvider The connection provider that will provide the relevant database connection (usually transaction bound)
     * @param aggregateIdentifier The aggregate identifier of the aggregate
     * @param <T> The type of the underlying aggregate class, not used by the framework, useful for type safety
     * @return The latest state of the aggregate
     */
    <T> Aggregate<T> loadAggregate(ConnectionProvider connectionProvider, UUID aggregateIdentifier);

    /**
     * Saves an aggregate's current state
     *
     * @param connectionProvider The connection provider that will provide the relevant database connection (usually transaction bound)
     * @param aggregate The aggregate to save
     * @return If the does not aggregate exist, the newly global id assigned when it's state was saved, otherwise, <code>aggregate.getGlobalId();</code>
     */
    long saveAggregate(ConnectionProvider connectionProvider, Aggregate<?> aggregate);

}
