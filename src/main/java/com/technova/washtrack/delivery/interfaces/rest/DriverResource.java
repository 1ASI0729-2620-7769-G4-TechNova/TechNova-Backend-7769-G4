package com.technova.washtrack.delivery.interfaces.rest;

import com.technova.washtrack.delivery.domain.model.Driver;

public record DriverResource(Long id, String name, boolean available) {

    static DriverResource from(Driver driver) {
        return new DriverResource(driver.id(), driver.name(), driver.isAvailable());
    }
}
