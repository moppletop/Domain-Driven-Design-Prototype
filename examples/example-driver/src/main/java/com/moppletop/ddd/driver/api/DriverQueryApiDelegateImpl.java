package com.moppletop.ddd.driver.api;

import com.moppletop.ddd.driver.api.v1.controller.DriverQueryApiDelegate;
import com.moppletop.ddd.driver.api.v1.model.Driver;
import com.moppletop.ddd.driver.database.DriverRepository;
import com.moppletop.ddd.driver.query.DriverQueryHandler;
import com.moppletop.ddd.driver.query.QueryNames;
import com.moppletop.ddd.query.QueryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverQueryApiDelegateImpl implements DriverQueryApiDelegate {

    private final QueryGateway queryGateway;

    @Override
    public ResponseEntity<Driver> getDriver(UUID driverId) {
        com.moppletop.ddd.driver.types.Driver driver = queryGateway.query(QueryNames.GET_DRIVER_BY_ID, driverId, com.moppletop.ddd.driver.types.Driver.class);

        if (driver == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new Driver()
                .driverId(driver.getDriverId())
                .name(driver.getName())
                .dateOfBirth(driver.getDateOfBirth())
        );
    }
}
