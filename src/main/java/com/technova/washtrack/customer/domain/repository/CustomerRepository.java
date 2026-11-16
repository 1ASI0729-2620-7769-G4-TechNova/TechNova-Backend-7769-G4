package com.technova.washtrack.customer.domain.repository;

import java.util.List;
import java.util.Optional;

import com.technova.washtrack.customer.domain.model.Customer;

public interface CustomerRepository {

    Long nextId();

    Long nextAddressId();

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    Optional<Customer> findByUserId(Long userId);

    /** All customers ordered by full name. */
    List<Customer> findAll();
}
