package com.technova.washtrack.customer.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

/**
 * Aggregate root of the customer context: the profile of a person who requests laundry services
 * and the addresses they reuse for pickups and deliveries.
 */
public class Customer {

    private final Long id;
    private final Long userId;
    private String fullName;
    private String phone;
    private final List<Address> addresses = new ArrayList<>();

    private Customer(Long id, Long userId, String fullName, String phone) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName.trim();
        this.phone = phone.trim();
    }

    /** Registers the profile of the user identified by {@code userId} (the user lives in another context). */
    public static Customer register(Long id, Long userId, String fullName, String phone) {
        requireProfile(fullName, phone);
        if (userId == null) {
            throw new BusinessRuleException("Customer must belong to a user");
        }
        return new Customer(id, userId, fullName, phone);
    }

    public void updateProfile(String fullName, String phone) {
        requireProfile(fullName, phone);
        this.fullName = fullName.trim();
        this.phone = phone.trim();
    }

    /** Saves a new address; the first one a customer saves becomes the default automatically. */
    public Address addAddress(Long addressId, String street, String district, String reference,
            String contactPhone) {
        Address address = new Address(addressId, street, district, reference, contactPhone);
        if (addresses.stream().anyMatch(a -> a.sameLocationAs(address.street(), address.district()))) {
            throw new BusinessRuleException("Address " + address.street() + ", " + address.district()
                    + " is already saved");
        }
        address.markDefault(addresses.isEmpty());
        addresses.add(address);
        return address;
    }

    /** Makes one address the default for new requests; the previous default is released. */
    public Address setDefaultAddress(Long addressId) {
        Address chosen = findAddress(addressId);
        addresses.forEach(a -> a.markDefault(false));
        chosen.markDefault(true);
        return chosen;
    }

    /** Removes an address; if it was the default, the oldest remaining one takes its place. */
    public void removeAddress(Long addressId) {
        Address removed = findAddress(addressId);
        addresses.remove(removed);
        if (removed.isDefault() && !addresses.isEmpty()) {
            addresses.get(0).markDefault(true);
        }
    }

    public Address findAddress(Long addressId) {
        return addresses.stream().filter(a -> a.id().equals(addressId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
    }

    private static void requireProfile(String fullName, String phone) {
        if (fullName == null || fullName.isBlank() || phone == null || phone.isBlank()) {
            throw new BusinessRuleException("Customer requires full name and phone");
        }
    }

    public Long id() {
        return id;
    }

    public Long userId() {
        return userId;
    }

    public String fullName() {
        return fullName;
    }

    public String phone() {
        return phone;
    }

    /** Addresses in the order they were saved. */
    public List<Address> addresses() {
        return Collections.unmodifiableList(addresses);
    }
}
