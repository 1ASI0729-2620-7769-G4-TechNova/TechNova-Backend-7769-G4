package com.technova.washtrack.delivery.interfaces.rest;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.delivery.application.DriverService;
import com.technova.washtrack.delivery.domain.model.Driver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/drivers")
@Tag(name = "Drivers", description = "Drivers available for pickups and deliveries")
public class DriverController {

    record RegisterDriverRequest(@NotBlank String name, String phone, String vehicle) {
    }

    record UpdateDriverRequest(@NotBlank String name, String phone, String vehicle, Boolean available) {
    }

    record AvailabilityRequest(@NotNull Boolean available) {
    }

    private final DriverService drivers;

    DriverController(DriverService drivers) {
        this.drivers = drivers;
    }

    @PostMapping
    @Operation(summary = "Register a driver (available by default)")
    ResponseEntity<DriverResource> register(@Valid @RequestBody RegisterDriverRequest request) {
        Driver driver = drivers.register(request.name(), request.phone(), request.vehicle());
        return ResponseEntity.created(URI.create("/api/v1/drivers/" + driver.id())).body(DriverResource.from(driver));
    }

    @GetMapping
    @Operation(summary = "List drivers by name; availableOnly=true hides unavailable ones")
    List<DriverResource> list(@RequestParam(defaultValue = "false") boolean availableOnly) {
        return drivers.list(availableOnly).stream().map(DriverResource::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a driver by id")
    DriverResource get(@PathVariable Long id) {
        return DriverResource.from(drivers.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a driver's name, phone, vehicle and optionally availability")
    DriverResource update(@PathVariable Long id, @Valid @RequestBody UpdateDriverRequest request) {
        return DriverResource.from(drivers.update(id, request.name(), request.phone(), request.vehicle(),
                request.available()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mark a driver as available or unavailable")
    DriverResource setAvailability(@PathVariable Long id, @Valid @RequestBody AvailabilityRequest request) {
        return DriverResource.from(drivers.setAvailability(id, request.available()));
    }
}
