package com.moppletop.ddd.error;

import com.moppletop.ddd.command.Command;

/**
 * When a command's {@link Command#getTargetAggregateIdentifier()} is null or somehow throws an exception
 */
public class NoTargetAggregateIdentifierException extends RuntimeException {

    public NoTargetAggregateIdentifierException(String message) {
        super(message);
    }

    public NoTargetAggregateIdentifierException(Throwable cause) {
        super(cause);
    }

}
