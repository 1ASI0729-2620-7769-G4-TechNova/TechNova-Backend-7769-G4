package com.technova.washtrack.delivery.interfaces.rest;

import com.technova.washtrack.delivery.domain.model.Driver;

public record DriverResource(Long id, String name, String phone, String vehicle, boolean available) {

    static DriverResource from(Driver driver) {
        return new DriverResource(driver.id(), driver.name(), driver.phone(), driver.vehicle(), driver.isAvailable());
    }
}
