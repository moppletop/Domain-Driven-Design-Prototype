package com.moppletop.ddd.driver.eventhandler;

import com.moppletop.ddd.event.ProcessingGroup;
import com.moppletop.ddd.event.StreamEventHandler;
import com.moppletop.ddd.driver.event.DriverNameAmended;
import com.moppletop.ddd.driver.event.DriverRegistered;
import com.moppletop.ddd.spring.WiredEventHandlers;
import lombok.extern.slf4j.Slf4j;

@WiredEventHandlers(domainEvents = false)
@ProcessingGroup(name = "drivers", threads = 2)
@Slf4j
public class DriverStreamEventHandler {

    @StreamEventHandler
    public void handle(DriverRegistered event) {
        log.info("Got an async event: {}", event);
    }

    @StreamEventHandler
    public void handle(DriverNameAmended event) {
        log.info("Got an async event: {}", event);
    }

}
