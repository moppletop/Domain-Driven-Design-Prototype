package com.moppletop.ddd.driver.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DriverRepository {

    private static final String GET_DRIVER_BY_ID = "select * from driver where driver_id = ?";

    private static final String INSERT_DRIVER = "insert into driver (driver_id, name, date_of_birth) values (?,?,?);";
    private static final String AMEND_DRIVER_NAME = "update driver set name = ? where driver_id = ?";

    private final DataSource dataSource;

    public Optional<DriverEntity> getDriverById(UUID driverId) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_DRIVER_BY_ID)) {
                statement.setObject(1, driverId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(DriverEntity.builder()
                                .id(resultSet.getLong("id"))
                                .driverId(resultSet.getObject("driver_id", UUID.class))
                                .name(resultSet.getString("name"))
                                .dateOfBirth(resultSet.getDate("date_of_birth").toLocalDate())
                                .build());
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public DriverEntity insertDriver(DriverEntity driverEntity) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT_DRIVER, Statement.RETURN_GENERATED_KEYS)) {
                statement.setObject(1, driverEntity.getDriverId());
                statement.setString(2, driverEntity.getName());
                statement.setDate(3, Date.valueOf(driverEntity.getDateOfBirth()));

                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return driverEntity.toBuilder()
                                .id(resultSet.getLong(1))
                                .build();
                    } else {
                        throw new SQLException("Failed to generate primary keys!");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void amendDriverName(UUID driverId, String name) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(AMEND_DRIVER_NAME)) {
                statement.setString(1, name);
                statement.setObject(2, driverId);

                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
