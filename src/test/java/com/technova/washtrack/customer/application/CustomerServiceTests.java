package com.technova.washtrack.customer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.technova.washtrack.customer.domain.model.Customer;
import com.technova.washtrack.shared.exception.ConflictException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

/** Each test uses its own user ids because the in-memory store is shared by the context. */
@SpringBootTest
class CustomerServiceTests {

    @Autowired
    private CustomerService customers;

    @Test
    void registersACustomerAndFindsItById() {
        Customer registered = customers.register(5001L, "Ana Martinez", "987654321");

        Customer found = customers.getById(registered.id());

        assertEquals(5001L, found.userId());
        assertEquals("Ana Martinez", found.fullName());
    }

    @Test
    void aUserCanOnlyHaveOneCustomerProfile() {
        customers.register(5002L, "Luis Quispe", "987654321");

        assertThrows(ConflictException.class, () -> customers.register(5002L, "Luis Otro", "912345678"));
        assertEquals(1, customers.list(5002L).size());
    }

    @Test
    void listFiltersByUserAndIsEmptyWhenThereIsNoProfile() {
        customers.register(5003L, "Rosa Diaz", "987654321");

        assertEquals("Rosa Diaz", customers.list(5003L).get(0).fullName());
        assertTrue(customers.list(5999L).isEmpty());
        assertTrue(customers.list(null).size() >= 1);
    }

    @Test
    void updatesTheProfileAndFailsForUnknownCustomers() {
        Customer customer = customers.register(5004L, "Mario Gomez", "987654321");

        Customer updated = customers.update(customer.id(), "Mario Gomez Ruiz", "912345678");

        assertEquals("Mario Gomez Ruiz", updated.fullName());
        assertThrows(ResourceNotFoundException.class, () -> customers.update(999999L, "Nadie", "912345678"));
    }

    @Test
    void managesAddressesAndTheDefaultOne() {
        Customer customer = customers.register(5005L, "Carla Soto", "987654321");
        customers.addAddress(customer.id(), "Av. Arequipa 123", "Miraflores", "Frente al parque", "987654321");
        customers.addAddress(customer.id(), "Calle Los Pinos 45", "San Isidro", null, "987654321");

        assertEquals("Av. Arequipa 123", customers.getDefaultAddress(customer.id()).street());

        Long secondId = customers.getById(customer.id()).addresses().get(1).id();
        customers.setDefaultAddress(customer.id(), secondId);
        assertEquals("Calle Los Pinos 45", customers.getDefaultAddress(customer.id()).street());

        customers.removeAddress(customer.id(), secondId);
        assertEquals("Av. Arequipa 123", customers.getDefaultAddress(customer.id()).street());
    }

    @Test
    void customerWithoutAddressesHasNoDefaultAddress() {
        Customer customer = customers.register(5006L, "Pedro Vega", "987654321");

        assertThrows(ResourceNotFoundException.class, () -> customers.getDefaultAddress(customer.id()));
    }
}
