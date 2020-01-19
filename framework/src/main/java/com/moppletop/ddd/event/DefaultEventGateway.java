package com.moppletop.ddd.event;

import com.moppletop.ddd.wiring.WiringManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultEventGateway implements EventGateway {

    private final WiringManager wiringManager;

    @Override
    public void handleStreamedEvent(EventContainer<?> eventContainer) throws Exception {
        wiringManager.executeStreamEventHandler(eventContainer.getPayload());
    }

}
