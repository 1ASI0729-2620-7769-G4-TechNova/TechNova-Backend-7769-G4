package com.technova.washtrack.delivery.domain.model;

/** A driver registered by the laundry who can be assigned to pickups and deliveries. */
public class Driver {

    private final Long id;
    private String name;
    private String phone = "";
    private String vehicle = "";
    private boolean available = true;

    public Driver(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void updateDetails(String name, String phone, String vehicle) {
        this.name = name;
        this.phone = phone == null ? "" : phone;
        this.vehicle = vehicle == null ? "" : vehicle;
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

    public String phone() {
        return phone;
    }

    public String vehicle() {
        return vehicle;
    }

    public boolean isAvailable() {
        return available;
    }
}
