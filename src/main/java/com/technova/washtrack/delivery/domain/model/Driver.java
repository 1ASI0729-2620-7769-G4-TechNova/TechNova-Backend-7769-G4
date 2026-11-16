package com.technova.washtrack.delivery.domain.model;

/** A driver registered by the laundry who can be assigned to pickups and deliveries. */
public class Driver {

    private final Long id;
    private final String name;
    private boolean available = true;

    public Driver(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }
}
