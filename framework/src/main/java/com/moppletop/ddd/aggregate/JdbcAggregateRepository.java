package com.moppletop.ddd.aggregate;

import com.moppletop.ddd.database.ConnectionProvider;
import com.moppletop.ddd.error.AggregateNotFoundException;
import com.moppletop.ddd.error.AggregateSerialisationException;
import com.moppletop.ddd.transformer.ObjectTransformer;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.UUID;

/**
 * The JDBC implementation of the aggregate repository
 */
@RequiredArgsConstructor
public class JdbcAggregateRepository implements AggregateRepository {

    private static final String GET_AGGREGATE_BY_ID = "select global_id, class_name, event_sequence, current_state from aggregate where aggregate_identifier = ?;";
    private static final String UPDATE_STATE = "update aggregate set current_state = ?, event_sequence = ? where global_id = ? and event_sequence = ?;";
    private static final String INSERT_STATE = "insert into aggregate (aggregate_identifier, class_name, event_sequence, current_state) values (?,?,?,?);";

    private final ObjectTransformer transformer;

    @Override
    public <T> Aggregate<T> loadAggregate(ConnectionProvider connectionProvider, UUID aggregateIdentifier) {
        Connection connection = connectionProvider.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(GET_AGGREGATE_BY_ID)) {
            statement.setObject(1, aggregateIdentifier);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long globalIndex = resultSet.getLong("global_id");
                    String className = resultSet.getString("class_name");
                    long eventSequence = resultSet.getLong("event_sequence");
                    String json = resultSet.getString("current_state");

                    return SimpleAggregate.<T>builder()
                            .globalId(globalIndex)
                            .aggregateIdentifier(aggregateIdentifier)
                            .className(className)
                            .initialEventSequence(eventSequence)
                            .eventSequence(eventSequence)
                            .currentState(deserialise(json, className))
                            .build();
                } else {
                    throw new AggregateNotFoundException("The aggregate with id " + aggregateIdentifier + " was not found.");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long saveAggregate(ConnectionProvider connectionProvider, Aggregate<?> aggregate) {
        Connection connection = connectionProvider.getConnection();
        String json = serialise(aggregate.getCurrentState());

        // If the aggregate already exists when we'll just update
        if (aggregate.exists()) {
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_STATE)) {
                statement.setString(1, json);
                statement.setLong(2, aggregate.getEventSequence());
                statement.setLong(3, aggregate.getGlobalId());
                statement.setLong(4, aggregate.getInitialEventSequence());

                // Since the query also checks that the "event_sequence" (last event sequence number) is equal to that of when
                // we got the aggregate, if no rows were updated, we know that the aggregate was modified while this node was
                // processing it
                if (statement.executeUpdate() == 0) {
                    throw new SQLException("Event sequence for aggregate: {" + aggregate + "} was modified during processing time.");
                }

                return aggregate.getGlobalId();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            // Otherwise insert a new row
            try (PreparedStatement statement = connection.prepareStatement(INSERT_STATE, Statement.RETURN_GENERATED_KEYS)) {
                statement.setObject(1, aggregate.getAggregateIdentifier());
                statement.setString(2, aggregate.getClassName());
                statement.setLong(3, aggregate.getEventSequence());
                statement.setString(4, json);

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Aggregate insertion did not generate a global id sequence!");
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialise(String json, String className) {
        try {
            return (T) transformer.deserialise(json, Class.forName(className));
        } catch (Exception ex) {
            throw new AggregateSerialisationException(ex);
        }
    }

    private String serialise(Object aggregate) {
        try {
            return transformer.serialise(aggregate);
        } catch (Exception ex) {
            throw new AggregateSerialisationException(ex);
        }
    }
}
