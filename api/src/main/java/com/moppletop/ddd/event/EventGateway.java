package com.moppletop.ddd.event;

/**
 * Represents the entry point streamers use for alerting the framework of a new event
 */
public interface EventGateway {

    /**
     * When an event has been received, the event streamer should call this
     *
     * @param eventContainer The contained event that is being handled
     * @throws Exception Any exception thrown here should be handled by the broker to mark that the event is a failure and
     * should be retried/dead lettered
     */
    void handleStreamedEvent(EventContainer<?> eventContainer) throws Exception;

}
