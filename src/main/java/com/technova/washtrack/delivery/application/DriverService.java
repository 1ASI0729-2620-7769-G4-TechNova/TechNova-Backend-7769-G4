package com.technova.washtrack.delivery.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.technova.washtrack.delivery.domain.model.Driver;
import com.technova.washtrack.delivery.domain.repository.DriverRepository;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class DriverService {

    private final DriverRepository drivers;

    DriverService(DriverRepository drivers) {
        this.drivers = drivers;
    }

    public Driver register(String name) {
        return drivers.save(new Driver(drivers.nextId(), name));
    }

    public Driver setAvailability(Long id, boolean available) {
        Driver driver = getById(id);
        driver.setAvailable(available);
        return drivers.save(driver);
    }

    public Driver getById(Long id) {
        return drivers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }

    /** Drivers ordered by name; when {@code onlyAvailable} is true, those marked unavailable are left out. */
    public List<Driver> list(boolean onlyAvailable) {
        return drivers.findAll().stream().filter(d -> !onlyAvailable || d.isAvailable()).toList();
    }
}
