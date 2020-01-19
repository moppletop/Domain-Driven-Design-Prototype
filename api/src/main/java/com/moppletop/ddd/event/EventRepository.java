package com.moppletop.ddd.event;

import com.moppletop.ddd.database.ConnectionProvider;

/**
 * Repository for storing event history
 */
public interface EventRepository {

    /**
     * @param connectionProvider The connection provider that will provide the relevant database connection (usually transaction bound)
     * @param eventContainer The event to be saved
     * @return The assigned event sequence number
     */
    long saveEvent(ConnectionProvider connectionProvider, EventContainer<?> eventContainer);

}
