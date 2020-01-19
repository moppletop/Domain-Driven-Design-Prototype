package com.moppletop.ddd.error;

/**
 * Thrown when a domain object (aggregate or handler) failed to be created due to invalid class structure, annotation configuration
 * or reflective security failures
 */
public class IllegalDomainDefinitionException extends RuntimeException {

    public IllegalDomainDefinitionException(String message) {
        super(message);
    }

    public IllegalDomainDefinitionException(Throwable cause) {
        super(cause);
    }
}
