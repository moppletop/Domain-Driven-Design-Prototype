package com.moppletop.ddd.command;

import java.util.UUID;

/**
 * @param <T> Not used by the framework, included for type-safety
 */
public interface Command<T> {

    /**
     * All Commands must return the target aggregate identifier they intend to act on, if this command is an "initial-state"
     * command, set the value to a random UUID
     */
    UUID getTargetAggregateIdentifier();

}
