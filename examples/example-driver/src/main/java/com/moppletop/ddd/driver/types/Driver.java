package com.moppletop.ddd.driver.types;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class Driver {

    UUID driverId;
    String name;
    LocalDate dateOfBirth;

}
