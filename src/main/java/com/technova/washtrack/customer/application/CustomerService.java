package com.technova.washtrack.customer.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.technova.washtrack.customer.domain.model.Address;
import com.technova.washtrack.customer.domain.model.Customer;
import com.technova.washtrack.customer.domain.repository.CustomerRepository;
import com.technova.washtrack.shared.exception.ConflictException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class CustomerService {

    private final CustomerRepository customers;

    CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    /** Registers the customer profile of a user; a user can only have one. */
    public Customer register(Long userId, String fullName, String phone) {
        if (customers.findByUserId(userId).isPresent()) {
            throw new ConflictException("User " + userId + " already has a customer profile");
        }
        return customers.save(Customer.register(customers.nextId(), userId, fullName, phone));
    }

    public Customer update(Long id, String fullName, String phone) {
        Customer customer = getById(id);
        customer.updateProfile(fullName, phone);
        return customers.save(customer);
    }

    public Customer getById(Long id) {
        return customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    /** Customers ordered by name; when {@code userId} is given, only the profile of that user. */
    public List<Customer> list(Long userId) {
        if (userId != null) {
            return customers.findByUserId(userId).map(List::of).orElse(List.of());
        }
        return customers.findAll();
    }

    public Customer addAddress(Long customerId, String street, String district, String reference,
            String contactPhone) {
        Customer customer = getById(customerId);
        customer.addAddress(customers.nextAddressId(), street, district, reference, contactPhone);
        return customers.save(customer);
    }

    public Customer setDefaultAddress(Long customerId, Long addressId) {
        Customer customer = getById(customerId);
        customer.setDefaultAddress(addressId);
        return customers.save(customer);
    }

    public Customer removeAddress(Long customerId, Long addressId) {
        Customer customer = getById(customerId);
        customer.removeAddress(addressId);
        return customers.save(customer);
    }

    /** The address new pickups and deliveries use when the customer does not pick another one. */
    public Address getDefaultAddress(Long customerId) {
        return getById(customerId).addresses().stream().filter(Address::isDefault).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Default address of customer", customerId));
    }
}
