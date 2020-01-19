package com.moppletop.ddd.query;

/**
 * Represents the entry point users of the API to issue query requests
 */
public interface QueryGateway {

    /**
     * @param key The queries key
     * @param queryParams The input parameter for the query
     * @param classOfT The class of the response
     * @param <T> The type of the response
     * @param <U> The type of the input parameter
     * @return The value the corresponding {@link QueryHandler}
     */
    <T, U> T query(String key, U queryParams, Class<T> classOfT);

}
