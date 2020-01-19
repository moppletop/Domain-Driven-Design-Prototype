package com.moppletop.ddd.driver.query;

import com.moppletop.ddd.driver.database.DriverRepository;
import com.moppletop.ddd.driver.types.Driver;
import com.moppletop.ddd.query.QueryHandler;
import com.moppletop.ddd.spring.WiredQueryHandlers;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@WiredQueryHandlers
@RequiredArgsConstructor
public class DriverQueryHandler {

    private final DriverRepository repository;

    @QueryHandler(QueryNames.GET_DRIVER_BY_ID)
    public Driver getDriverById(UUID driverId) {
        return repository.getDriverById(driverId)
                .map(entity -> Driver.builder()
                        .driverId(entity.getDriverId())
                        .name(entity.getName())
                        .dateOfBirth(entity.getDateOfBirth())
                        .build())
                .orElse(null);
    }
}
