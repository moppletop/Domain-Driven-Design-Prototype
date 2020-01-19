package com.moppletop.ddd.query;

import com.moppletop.ddd.wiring.WiringManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultQueryGateway implements QueryGateway {

    private final WiringManager wiringManager;

    @Override
    public <T, U> T query(String key, U queryParams, Class<T> classOfT) {
        return wiringManager.executeQueryHandler(key, queryParams, classOfT);
    }
}
