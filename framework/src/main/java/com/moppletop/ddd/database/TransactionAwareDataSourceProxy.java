package com.moppletop.ddd.database;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * A {@link Transaction} aware datasource, useful to integrating with libraries such as JPA for your business models.
 * Means you don't need to mess around with getting other libraries to play ball with {@link ConnectionProvider}.
 *
 * Beware that while in a transaction, a connection's commit(), rollback() and close() operations will silently do nothing,
 * the overarching transaction should handle that
 */
@RequiredArgsConstructor
public class TransactionAwareDataSourceProxy implements DataSource {

    private final DataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException {
        if (Transaction.inTransaction()) {
            return new TransactionAwareConnectionProxy(Transaction.get().getConnection());
        }

        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) {
        throw new UnsupportedOperationException("Getting a connection with a username and password is not supported!");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
}
