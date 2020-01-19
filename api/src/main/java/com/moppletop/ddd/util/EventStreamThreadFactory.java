package com.moppletop.ddd.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A useful thread factory for setting up the threads to stream events through
 */
public class EventStreamThreadFactory implements ThreadFactory {

    private final String processingGroup;
    private final AtomicInteger count;

    public EventStreamThreadFactory(String processingGroup) {
        this.processingGroup = processingGroup;
        this.count = new AtomicInteger();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);

        thread.setName("es-" + processingGroup + "-" + count.incrementAndGet());

        return thread;
    }

}
