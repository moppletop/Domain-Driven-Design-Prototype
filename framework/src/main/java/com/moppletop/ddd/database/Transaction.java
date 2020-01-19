package com.moppletop.ddd.database;

import lombok.Getter;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is the big one, the transaction class.
 * This handles the management of the command -> aggregate -> event flow, ensure that all of these
 * happen within the same sql transaction.
 * <br>
 * <br>
 * Implementation notes:
 * <ul>
 *     <li>Transactions are thread bound to the thread</li>
 *     <li>Nested transactions are not supported</li>
 * </ul>
 */
@Getter
public class Transaction implements ConnectionProvider {

    private static final ThreadLocal<Transaction> CURRENT_TRANSACTION = ThreadLocal.withInitial(Transaction::new);

    /**
     * @return The current (active) transaction for this thread, otherwise an {@link IllegalStateException} is thrown
     */
    public static Transaction get() {
        Transaction current = CURRENT_TRANSACTION.get();

        if (current.hasStarted()) {
            return current;
        }

        throw new IllegalStateException("Tried to get the current transaction but this thread is does not have an active transaction!");
    }

    /**
     * @return true if this thread is current has an active transaction
     */
    public static boolean inTransaction() {
        return CURRENT_TRANSACTION.get().hasStarted();
    }

    /**
     * @return An inactive stateless transaction
     */
    public static Transaction statelessTransaction() {
        return new Transaction();
    }

    /**
     * @param dataSource The database that will provide the connection
     * @return The newly started transaction
     */
    @SneakyThrows(SQLException.class)
    public static Transaction createAndStart(DataSource dataSource) {
        Transaction current = CURRENT_TRANSACTION.get();

        if (current.hasStarted()) {
            throw new IllegalStateException("Nested transactions are not supported!");
        }

        current.start(dataSource);
        return current;
    }

    /**
     * Stops the current transaction, and attempts to commit the changes, if the commit fails, the transaction will roll
     * back, the exception will be throw up the stack
     */
    @SneakyThrows(SQLException.class)
    public static void stopAndCommit() {
        Transaction current = CURRENT_TRANSACTION.get();

        if (!current.hasStarted()) {
            throw new IllegalStateException("Tried to stop the current transaction but this thread is does not have an active transaction!");
        }

        CURRENT_TRANSACTION.remove();
        current.stop();
    }

    /**
     * Stops the current transaction, and attempts to rollback the changes
     */
    @SneakyThrows(SQLException.class)
    public static void stopAndRollback() {
        Transaction current = CURRENT_TRANSACTION.get();

        if (!current.hasStarted()) {
            throw new IllegalStateException("Tried to stop the current transaction but this thread is does not have an active transaction!");
        }

        CURRENT_TRANSACTION.remove();
        current.rollback();
    }

    /**
     * Executes a runnable if a transaction is currently active
     * @param runnable The runnable to execute
     */
    public static void executeIfInTransaction(Runnable runnable) {
        Transaction current = CURRENT_TRANSACTION.get();

        if (current.hasStarted()) {
            runnable.run();
        }
    }

    private Connection connection;

    private void start(DataSource dataSource) throws SQLException {
        this.connection = dataSource.getConnection();
    }

    private void stop() throws SQLException {
        try {
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.close();
        }
    }

    private void rollback() throws SQLException {
        try {
            connection.rollback();
        } finally {
            connection.close();
        }
    }

    public boolean hasStarted() {
        return connection != null;
    }
}
