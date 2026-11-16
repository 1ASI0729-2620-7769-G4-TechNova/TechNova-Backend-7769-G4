package com.technova.washtrack.customer.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.technova.washtrack.customer.domain.model.Customer;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

class CustomerTests {

    private static Customer customer() {
        return Customer.register(1L, 10L, "  Ana Martinez ", " 987654321 ");
    }

    @Test
    void registersTrimmedProfileWithoutAddresses() {
        Customer customer = customer();

        assertEquals("Ana Martinez", customer.fullName());
        assertEquals("987654321", customer.phone());
        assertTrue(customer.addresses().isEmpty());
    }

    @Test
    void rejectsMissingNamePhoneOrUser() {
        assertThrows(BusinessRuleException.class, () -> Customer.register(1L, 10L, " ", "987654321"));
        assertThrows(BusinessRuleException.class, () -> Customer.register(1L, 10L, "Ana", null));
        assertThrows(BusinessRuleException.class, () -> Customer.register(1L, null, "Ana", "987654321"));
    }

    @Test
    void updatesProfile() {
        Customer customer = customer();

        customer.updateProfile("Ana Perez", "912345678");

        assertEquals("Ana Perez", customer.fullName());
        assertEquals("912345678", customer.phone());
        assertThrows(BusinessRuleException.class, () -> customer.updateProfile("", "912345678"));
    }

    @Test
    void firstAddressIsDefaultAndTheNextOnesAreNot() {
        Customer customer = customer();

        var home = customer.addAddress(1L, "Av. Arequipa 123", "Miraflores", "Frente al parque", "987654321");
        var office = customer.addAddress(2L, "Calle Los Pinos 45", "San Isidro", null, "987654321");

        assertTrue(home.isDefault());
        assertFalse(office.isDefault());
        assertNull(office.reference());
    }

    @Test
    void rejectsIncompleteOrDuplicatedAddresses() {
        Customer customer = customer();
        customer.addAddress(1L, "Av. Arequipa 123", "Miraflores", null, "987654321");

        assertThrows(BusinessRuleException.class, () -> customer.addAddress(2L, " ", "Miraflores", null, "987654321"));
        assertThrows(BusinessRuleException.class, () -> customer.addAddress(2L, "Av. Arequipa 123", "", null, "987654321"));
        assertThrows(BusinessRuleException.class,
                () -> customer.addAddress(2L, "av. arequipa 123", "MIRAFLORES", null, "987654321"));
        assertEquals(1, customer.addresses().size());
    }

    @Test
    void onlyOneAddressIsDefaultAtATime() {
        Customer customer = customer();
        customer.addAddress(1L, "Av. Arequipa 123", "Miraflores", null, "987654321");
        customer.addAddress(2L, "Calle Los Pinos 45", "San Isidro", null, "987654321");

        customer.setDefaultAddress(2L);

        assertFalse(customer.findAddress(1L).isDefault());
        assertTrue(customer.findAddress(2L).isDefault());
        assertThrows(ResourceNotFoundException.class, () -> customer.setDefaultAddress(99L));
    }

    @Test
    void removingTheDefaultPromotesTheOldestRemaining() {
        Customer customer = customer();
        customer.addAddress(1L, "Av. Arequipa 123", "Miraflores", null, "987654321");
        customer.addAddress(2L, "Calle Los Pinos 45", "San Isidro", null, "987654321");
        customer.addAddress(3L, "Jr. Lima 10", "Barranco", null, "987654321");

        customer.removeAddress(1L);

        assertEquals(2, customer.addresses().size());
        assertTrue(customer.findAddress(2L).isDefault());
        assertFalse(customer.findAddress(3L).isDefault());

        customer.removeAddress(3L);
        assertTrue(customer.findAddress(2L).isDefault());
        assertThrows(ResourceNotFoundException.class, () -> customer.removeAddress(99L));
    }
}
