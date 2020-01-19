package com.moppletop.ddd.error;

/**
 * Thrown when an event could not be serialised/deserialised
 */
public class EventSerialisationException extends RuntimeException {

    public EventSerialisationException(String message) {
        super(message);
    }

    public EventSerialisationException(Throwable cause) {
        super(cause);
    }

}
