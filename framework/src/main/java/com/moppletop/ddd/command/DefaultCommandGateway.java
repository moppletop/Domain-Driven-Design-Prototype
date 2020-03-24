package com.moppletop.ddd.command;

import com.moppletop.ddd.aggregate.Aggregate;
import com.moppletop.ddd.aggregate.AggregateRepository;
import com.moppletop.ddd.aggregate.AggregateState;
import com.moppletop.ddd.aggregate.SimpleAggregate;
import com.moppletop.ddd.error.AggregateNotFoundException;
import com.moppletop.ddd.error.NoTargetAggregateIdentifierException;
import com.moppletop.ddd.event.EventContainer;
import com.moppletop.ddd.event.EventMetadata;
import com.moppletop.ddd.event.EventRepository;
import com.moppletop.ddd.database.Transaction;
import com.moppletop.ddd.wiring.WiringManager;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The default implementation of the CommandGateway
 */
@RequiredArgsConstructor
public class DefaultCommandGateway implements CommandGateway {

    // Map target aggregate id to a lock
    private static final Map<UUID, UUID> AGGREGATE_LOCKS = new WeakHashMap<>();

    protected static Object getSynchronisedObjectFor(UUID targetAggregateId) {
        synchronized (AGGREGATE_LOCKS) {
            UUID lock = AGGREGATE_LOCKS.get(targetAggregateId);

            if (lock == null) {
                lock = new UUID(targetAggregateId.getMostSignificantBits(), targetAggregateId.getLeastSignificantBits());
                AGGREGATE_LOCKS.put(lock, lock);
            }

            return lock;
        }
    }

    private final DataSource dataSource;

    private final AggregateRepository aggregateRepository;
    private final EventRepository eventRepository;
    private final WiringManager wiringManager;

    @Override
    public <T> void send(Command<T> cmd) {
        if (Transaction.inTransaction()) {
            throw new IllegalStateException("Attempted to send a command while in a transaction (sending another command)");
        }

        UUID targetAggregateId = getTargetAggregateIdentifier(cmd);

        try {
            // Lock here to prevent multiple threads trying to issue commands against the same
            // aggregate
            synchronized (getSynchronisedObjectFor(targetAggregateId)) {
                // Wrap the sending of the command in a transaction
                try {
                    Transaction.createAndStart(dataSource);
                    unsafeSend(targetAggregateId, cmd);
                    Transaction.stopAndCommit();
                } catch (Exception ex) {
                    Transaction.executeIfInTransaction(Transaction::stopAndRollback);
                    throw ex;
                }
            }
        } finally {
            AGGREGATE_LOCKS.remove(targetAggregateId);
        }
    }

    // A safe way to get the target aggregate identifier
    private UUID getTargetAggregateIdentifier(Command<?> cmd) {
        UUID targetAggregateId;

        try {
            targetAggregateId = cmd.getTargetAggregateIdentifier();
        } catch (Exception ex) {
            throw new NoTargetAggregateIdentifierException(ex);
        }

        if (targetAggregateId == null) {
            throw new NoTargetAggregateIdentifierException("Commands must implement Command. The getTargetAggregateIdentifier() method must return a non-null value!");
        }

        return targetAggregateId;
    }

    // Actually sends the command to the aggregate
    // By "unsafe", it means that all transactions/threading/exception handling must be handled outside of this method,
    // as this method will not do anything like that
    private <T> void unsafeSend(UUID targetAggregateId, Command<T> cmd) {
        Transaction transaction = Transaction.get();

        Aggregate<T> aggregate;
        T aggregateInstance;

        try {
            // Attempt to load the aggregate from the repository
            aggregate = aggregateRepository.loadAggregate(transaction, targetAggregateId);
            aggregateInstance = aggregate.getCurrentState();
        } catch (AggregateNotFoundException ex) {
            // If the aggregate didn't exist, this is it's first command
            aggregate = null;
            aggregateInstance = null;
        }

        // TODO add command interceptors
        // TODO make use of metadata
        EventMetadata metadata = EventMetadata.empty();

        AggregateState.startTransaction(targetAggregateId);

        aggregateInstance = wiringManager.executeCommandHandler(aggregateInstance, cmd);

        // The aggregate will now definitely exist, since the command handler has been executed
        if (aggregate == null) {
            aggregate = SimpleAggregate.<T>builder()
                    .aggregateIdentifier(targetAggregateId)
                    .className(aggregateInstance.getClass().getName())
                    .build();
        }

        List<Object> events = AggregateState.stopTransaction();

        // No events, no need to update state
        if (events.isEmpty()) {
            return;
        }

        long globalId = aggregate.getGlobalId();

        // If the aggregate didn't exist
        if (!aggregate.exists()) {
            // We'll create an initial "empty" state for the aggregate so we can get a global id, since the events will
            // require it
            globalId = aggregateRepository.saveAggregate(transaction, aggregate);
        }

        long lastEventSequence = -1;

        for (Object event : events) {
            // TODO is it an issue we don't know the event id yet when applying the handlers?
            EventContainer<?> eventContainer = new EventContainer<>(-1L, globalId, event, metadata);

            wiringManager.executeAggregateStateHandler(aggregateInstance, event);
            wiringManager.executeDomainEventHandler(event);
            lastEventSequence = eventRepository.saveEvent(transaction, eventContainer);
        }

        aggregate = SimpleAggregate.<T>builder()
                .globalId(globalId)
                .aggregateIdentifier(targetAggregateId)
                .className(aggregateInstance.getClass().getName())
                .initialEventSequence(aggregate.getInitialEventSequence())
                .eventSequence(lastEventSequence)
                .currentState(aggregateInstance)
                .build();

        aggregateRepository.saveAggregate(transaction, aggregate);
    }
}