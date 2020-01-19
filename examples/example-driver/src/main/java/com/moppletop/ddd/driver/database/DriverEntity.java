package com.moppletop.ddd.driver.database;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class DriverEntity {

    @EqualsAndHashCode.Include
    Long id;

    UUID driverId;
    String name;
    LocalDate dateOfBirth;

}
