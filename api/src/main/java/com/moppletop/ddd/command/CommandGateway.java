package com.moppletop.ddd.command;

/**
 * Represents the entry point users of the API to issue commands
 */
public interface CommandGateway {

    /**
     * Dispatches a command synchronously. This will result an aggregate to be created/loaded and it's {@link com.moppletop.ddd.command.CommandHandler}
     * for this {@link Command} will be called.
     *
     * @param cmd The command being sent
     * @param <T> The type of the aggregate this command is targeted at
     */
    <T> void send(Command<T> cmd);

}
