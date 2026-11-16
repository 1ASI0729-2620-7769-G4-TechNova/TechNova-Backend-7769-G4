package com.technova.washtrack.customer.interfaces.rest;

import java.util.List;

import com.technova.washtrack.customer.domain.model.Customer;

public record CustomerResource(Long id, Long userId, String fullName, String phone,
        List<AddressResource> addresses) {

    static CustomerResource from(Customer customer) {
        return new CustomerResource(customer.id(), customer.userId(), customer.fullName(), customer.phone(),
                customer.addresses().stream().map(AddressResource::from).toList());
    }
}
