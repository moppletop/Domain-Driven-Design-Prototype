package com.moppletop.ddd.driver.event;

import lombok.Value;

import java.util.UUID;

@Value
public class DriverNameAmended {

    UUID driverId;
    String name;

}
