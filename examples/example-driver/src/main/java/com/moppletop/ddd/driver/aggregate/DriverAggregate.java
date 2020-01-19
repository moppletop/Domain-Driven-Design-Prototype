package com.moppletop.ddd.driver.aggregate;

import com.moppletop.ddd.aggregate.AggregateStateHandler;
import com.moppletop.ddd.command.CommandHandler;
import com.moppletop.ddd.driver.command.AmendDriverName;
import com.moppletop.ddd.driver.command.RegisterDriver;
import com.moppletop.ddd.driver.event.DriverNameAmended;
import com.moppletop.ddd.driver.event.DriverRegistered;
import com.moppletop.ddd.spring.WiredAggregate;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

import static com.moppletop.ddd.aggregate.AggregateState.apply;

@WiredAggregate
@NoArgsConstructor
public class DriverAggregate {

    private UUID id;
    private String name;
    private LocalDate dateOfBirth;

    @CommandHandler
    public DriverAggregate(RegisterDriver cmd) {
        apply(new DriverRegistered(cmd.getId(), cmd.getName(), cmd.getDateOfBirth()));
    }

    @CommandHandler
    public void amendName(AmendDriverName cmd) {
        apply(new DriverNameAmended(cmd.getId(), cmd.getName()));
    }

    @AggregateStateHandler
    private void on(DriverRegistered evt) {
        id = evt.getDriverId();
        name = evt.getName();
        dateOfBirth = evt.getDateOfBirth();
    }

    @AggregateStateHandler
    private void on(DriverNameAmended evt) {
        name = evt.getName();
    }
}
