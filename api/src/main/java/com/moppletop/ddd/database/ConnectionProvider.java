package com.moppletop.ddd.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Due to the nature of event/aggregate state and business tables being updated synchronously, we have to ensure that the
 * same database connection is used (with auto-commit off) during the command -> aggregate -> event flow in to preserve
 * "transactional-ity"
 */
public interface ConnectionProvider {

    /**
     * @return The connection tied to the current transaction
     */
    Connection getConnection();

}
