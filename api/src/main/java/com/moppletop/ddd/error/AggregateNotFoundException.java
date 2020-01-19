package com.moppletop.ddd.error;

/**
 * Thrown when there is no record of an aggregate matching given id(s), or when an "initial-state" command is send to an
 * aggregate that already exists or vice-versa
 */
public class AggregateNotFoundException extends RuntimeException {

    public AggregateNotFoundException(String message) {
        super(message);
    }

    public AggregateNotFoundException(Throwable cause) {
        super(cause);
    }

}
