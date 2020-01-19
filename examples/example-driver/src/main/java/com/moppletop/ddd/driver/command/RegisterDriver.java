package com.moppletop.ddd.driver.command;

import com.moppletop.ddd.command.Command;
import com.moppletop.ddd.driver.aggregate.DriverAggregate;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class RegisterDriver implements Command<DriverAggregate> {

    UUID id;
    String name;
    LocalDate dateOfBirth;

    @Override
    public UUID getTargetAggregateIdentifier() {
        return id;
    }
}
