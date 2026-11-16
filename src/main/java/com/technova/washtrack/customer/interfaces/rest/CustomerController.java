package com.technova.washtrack.customer.interfaces.rest;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.customer.application.CustomerService;
import com.technova.washtrack.customer.domain.model.Customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Customer profiles and their pickup and delivery addresses")
public class CustomerController {

    private final CustomerService customers;

    CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @PostMapping
    @Operation(summary = "Register a customer profile for a user (one per user)")
    ResponseEntity<CustomerResource> register(@Valid @RequestBody RegisterCustomerRequest request) {
        Customer customer = customers.register(request.userId(), request.fullName(), request.phone());
        return ResponseEntity.created(URI.create("/api/v1/customers/" + customer.id()))
                .body(CustomerResource.from(customer));
    }

    @GetMapping
    @Operation(summary = "List customers by name; userId returns only the profile of that user")
    List<CustomerResource> list(@RequestParam(required = false) Long userId) {
        return customers.list(userId).stream().map(CustomerResource::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer with its addresses")
    CustomerResource get(@PathVariable Long id) {
        return CustomerResource.from(customers.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the full name and phone of a customer")
    CustomerResource update(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest request) {
        return CustomerResource.from(customers.update(id, request.fullName(), request.phone()));
    }

    @PostMapping("/{id}/addresses")
    @Operation(summary = "Save an address; the first one saved becomes the default")
    ResponseEntity<CustomerResource> addAddress(@PathVariable Long id,
            @Valid @RequestBody AddAddressRequest request) {
        Customer customer = customers.addAddress(id, request.street(), request.district(), request.reference(),
                request.contactPhone());
        return ResponseEntity.created(URI.create("/api/v1/customers/" + id)).body(CustomerResource.from(customer));
    }

    @PostMapping("/{id}/addresses/{addressId}/default")
    @Operation(summary = "Mark an address as the default for pickups and deliveries")
    CustomerResource setDefaultAddress(@PathVariable Long id, @PathVariable Long addressId) {
        return CustomerResource.from(customers.setDefaultAddress(id, addressId));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    @Operation(summary = "Remove an address; if it was the default, the oldest remaining one takes its place")
    ResponseEntity<Void> removeAddress(@PathVariable Long id, @PathVariable Long addressId) {
        customers.removeAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }
}
