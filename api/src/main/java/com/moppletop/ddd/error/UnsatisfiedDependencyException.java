package com.moppletop.ddd.error;

import com.moppletop.ddd.dependency.DependencyRegister;

/**
 * Thrown when a Command/AggregateState/Event handler required an object but the
 * {@link DependencyRegister} did not have a record of it
 */
public class UnsatisfiedDependencyException extends RuntimeException {

    public UnsatisfiedDependencyException(String message) {
        super(message);
    }

    public UnsatisfiedDependencyException(Throwable cause) {
        super(cause);
    }

}
