package com.moppletop.ddd.event;

import com.moppletop.ddd.database.ConnectionProvider;
import com.moppletop.ddd.error.EventSerialisationException;
import com.moppletop.ddd.transformer.ObjectTransformer;
import lombok.RequiredArgsConstructor;

import java.sql.*;

/**
 * The JDBC implementation of the event repository
 */
@RequiredArgsConstructor
public class JdbcEventRepository implements EventRepository {

    private static final String INSERT_EVENT = "insert into event (aggregate_global_id, class_name, payload, metadata) values (?,?,?,?)";

    private final ObjectTransformer transformer;

    @Override
    public long saveEvent(ConnectionProvider connectionProvider, EventContainer<?> eventContainer) {
        Connection connection = connectionProvider.getConnection();
        String payload = serialise(eventContainer.getPayload());
        String metadata = serialise(eventContainer.getMetadata());

        try (PreparedStatement statement = connection.prepareStatement(INSERT_EVENT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, eventContainer.getGlobalAggregateId());
            statement.setString(2, eventContainer.getEventName());
            statement.setString(3, payload);
            statement.setString(4, metadata);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Event insertion did not generate a global sequence!");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String serialise(Object aggregate) {
        try {
            return transformer.serialise(aggregate);
        } catch (Exception ex) {
            throw new EventSerialisationException(ex);
        }
    }
}
