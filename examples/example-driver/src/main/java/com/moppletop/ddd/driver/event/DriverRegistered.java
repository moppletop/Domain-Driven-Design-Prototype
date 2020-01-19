package com.moppletop.ddd.driver.event;

import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class DriverRegistered {

    UUID driverId;
    String name;
    LocalDate dateOfBirth;

}
