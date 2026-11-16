package com.technova.washtrack.customer.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.technova.washtrack.customer.domain.model.Customer;
import com.technova.washtrack.customer.domain.repository.CustomerRepository;

/** Volatile storage used until the database is connected; replaced by a JPA adapter. */
@Repository
class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<Long, Customer> customers = new ConcurrentHashMap<>();
    private final AtomicLong customerSequence = new AtomicLong();
    private final AtomicLong addressSequence = new AtomicLong();

    @Override
    public Long nextId() {
        return customerSequence.incrementAndGet();
    }

    @Override
    public Long nextAddressId() {
        return addressSequence.incrementAndGet();
    }

    @Override
    public Customer save(Customer customer) {
        customers.put(customer.id(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public Optional<Customer> findByUserId(Long userId) {
        return customers.values().stream().filter(c -> c.userId().equals(userId)).findFirst();
    }

    @Override
    public List<Customer> findAll() {
        return customers.values().stream().sorted(Comparator.comparing(Customer::fullName)).toList();
    }
}
