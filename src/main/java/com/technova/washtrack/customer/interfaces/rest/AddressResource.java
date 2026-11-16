package com.technova.washtrack.customer.interfaces.rest;

import com.technova.washtrack.customer.domain.model.Address;

public record AddressResource(Long id, String street, String district, String reference, String contactPhone,
        boolean defaultAddress) {

    static AddressResource from(Address address) {
        return new AddressResource(address.id(), address.street(), address.district(), address.reference(),
                address.contactPhone(), address.isDefault());
    }
}
