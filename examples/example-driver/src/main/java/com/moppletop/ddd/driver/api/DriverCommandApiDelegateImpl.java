package com.moppletop.ddd.driver.api;

import com.moppletop.ddd.command.CommandGateway;
import com.moppletop.ddd.driver.api.v1.controller.DriverCommandApiDelegate;
import com.moppletop.ddd.driver.api.v1.model.AmendDriverDto;
import com.moppletop.ddd.driver.api.v1.model.Driver;
import com.moppletop.ddd.driver.api.v1.model.RegisterDriverDto;
import com.moppletop.ddd.driver.command.AmendDriverName;
import com.moppletop.ddd.driver.command.RegisterDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverCommandApiDelegateImpl implements DriverCommandApiDelegate {

    private final CommandGateway commandGateway;

    @Override
    public ResponseEntity<Driver> registerDriver(RegisterDriverDto registerDriverDto) {
        RegisterDriver cmd = new RegisterDriver(
                UUID.randomUUID(),
                registerDriverDto.getName(),
                registerDriverDto.getDateOfBirth()
        );

        commandGateway.send(cmd);

        return ResponseEntity.ok(new Driver()
                .driverId(cmd.getId())
                .name(cmd.getName())
                .dateOfBirth(cmd.getDateOfBirth())
        );
    }

    @Override
    public ResponseEntity<Void> amendDriver(AmendDriverDto amendDriverDto) {
        AmendDriverName cmd = new AmendDriverName(
                amendDriverDto.getDriverId(),
                amendDriverDto.getName()
        );

        commandGateway.send(cmd);

        return ResponseEntity.ok().build();
    }
}
