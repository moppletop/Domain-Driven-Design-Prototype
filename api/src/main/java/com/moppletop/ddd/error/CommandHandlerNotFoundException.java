package com.moppletop.ddd.error;

/**
 * Thrown when a command is dispatched but no @{@link com.moppletop.ddd.command.CommandHandler} could be found
 */
public class CommandHandlerNotFoundException extends RuntimeException {

    public CommandHandlerNotFoundException(String message) {
        super(message);
    }

}
