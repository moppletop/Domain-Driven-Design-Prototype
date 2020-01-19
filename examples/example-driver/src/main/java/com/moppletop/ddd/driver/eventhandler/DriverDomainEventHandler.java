package com.moppletop.ddd.driver.eventhandler;

import com.moppletop.ddd.driver.database.DriverEntity;
import com.moppletop.ddd.driver.database.DriverRepository;
import com.moppletop.ddd.driver.event.DriverNameAmended;
import com.moppletop.ddd.driver.event.DriverRegistered;
import com.moppletop.ddd.event.DomainEventHandler;
import com.moppletop.ddd.spring.WiredEventHandlers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@WiredEventHandlers(streamEvents = false)
@RequiredArgsConstructor
@Slf4j
public class DriverDomainEventHandler {

    private final DriverRepository repository;

    @DomainEventHandler
    public void handle(DriverRegistered event) {
        log.info("Driver Registered: {}", event);

        repository.insertDriver(DriverEntity.builder()
                .driverId(event.getDriverId())
                .name(event.getName())
                .dateOfBirth(event.getDateOfBirth())
                .build());
    }

    @DomainEventHandler
    public void handle(DriverNameAmended event) {
        log.info("Driver Amended: {}", event);

        repository.amendDriverName(event.getDriverId(), event.getName());
    }
}
