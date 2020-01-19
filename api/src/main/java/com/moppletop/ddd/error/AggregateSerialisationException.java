package com.moppletop.ddd.error;

/**
 * Thrown when an aggregate's state could not be serialised/deserialised
 */
public class AggregateSerialisationException extends RuntimeException {

    public AggregateSerialisationException(String message) {
        super(message);
    }

    public AggregateSerialisationException(Throwable cause) {
        super(cause);
    }

}
