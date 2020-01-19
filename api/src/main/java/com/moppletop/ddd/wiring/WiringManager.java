package com.moppletop.ddd.wiring;

import com.moppletop.ddd.command.Command;

/**
 * The WiringManager is responsible for managing creation and execution of all aggregates and handlers via reflection or
 * other means
 */
public interface WiringManager {

    <T> T executeCommandHandler(T aggregate, Command<T> cmd);

    void executeAggregateStateHandler(Object aggregate, Object event);

    void executeDomainEventHandler(Object event);

    void executeStreamEventHandler(Object event);

    <I, O> O executeQueryHandler(String key, I input, Class<O> classOfOutput);

}
