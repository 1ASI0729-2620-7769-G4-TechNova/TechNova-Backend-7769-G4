package com.technova.washtrack.customer.domain.model;

import com.technova.washtrack.shared.exception.BusinessRuleException;

/** A pickup or delivery address saved by a customer. It only exists inside its {@link Customer}. */
public class Address {

    private final Long id;
    private final String street;
    private final String district;
    private final String reference;
    private final String contactPhone;
    private boolean defaultAddress;

    Address(Long id, String street, String district, String reference, String contactPhone) {
        if (isBlank(street) || isBlank(district) || isBlank(contactPhone)) {
            throw new BusinessRuleException("Address requires street, district and contact phone");
        }
        this.id = id;
        this.street = street.trim();
        this.district = district.trim();
        this.reference = isBlank(reference) ? null : reference.trim();
        this.contactPhone = contactPhone.trim();
    }

    boolean sameLocationAs(String otherStreet, String otherDistrict) {
        return street.equalsIgnoreCase(otherStreet.trim()) && district.equalsIgnoreCase(otherDistrict.trim());
    }

    void markDefault(boolean value) {
        this.defaultAddress = value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long id() {
        return id;
    }

    public String street() {
        return street;
    }

    public String district() {
        return district;
    }

    public String reference() {
        return reference;
    }

    public String contactPhone() {
        return contactPhone;
    }

    public boolean isDefault() {
        return defaultAddress;
    }
}
