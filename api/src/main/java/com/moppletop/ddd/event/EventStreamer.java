package com.moppletop.ddd.event;

/**
 * Represents a streamer/broker of events
 */
public interface EventStreamer {

    /**
     * Subscribes to a group to process events
     *
     * @param processingGroup the name of the processing group
     * @param threads the number of threads that will consumer events
     */
    void subscribe(String processingGroup, int threads);

    /**
     * Begins the shutdown process, all threads will exit after they've processed the current event
     */
    void shutdown();

}
