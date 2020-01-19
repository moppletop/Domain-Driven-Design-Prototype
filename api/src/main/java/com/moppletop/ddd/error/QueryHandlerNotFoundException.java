package com.moppletop.ddd.error;

/**
 * Thrown when a query is dispatched but no @{@link com.moppletop.ddd.query.QueryHandler} could be found
 */
public class QueryHandlerNotFoundException extends RuntimeException {

    public QueryHandlerNotFoundException(String message) {
        super(message);
    }

    public QueryHandlerNotFoundException(Throwable cause) {
        super(cause);
    }

}
