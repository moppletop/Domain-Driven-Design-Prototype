package com.moppletop.ddd.driver.command;

import com.moppletop.ddd.command.Command;
import com.moppletop.ddd.driver.aggregate.DriverAggregate;
import lombok.Value;

import java.util.UUID;

@Value
public class AmendDriverName implements Command<DriverAggregate> {

    UUID id;
    String name;

    @Override
    public UUID getTargetAggregateIdentifier() {
        return id;
    }
}
