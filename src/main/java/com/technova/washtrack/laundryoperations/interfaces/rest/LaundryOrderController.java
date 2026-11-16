package com.technova.washtrack.laundryoperations.interfaces.rest;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.laundryoperations.application.LaundryOrderService;
import com.technova.washtrack.laundryoperations.domain.model.LaundryOrder;
import com.technova.washtrack.laundryoperations.domain.model.LaundryStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Paths keep the collection name used by the frontend (laundryOrders). */
@RestController
@RequestMapping("/api/v1/laundryOrders")
@Tag(name = "Laundry Operations", description = "Reception and internal processing stages of laundry orders")
public class LaundryOrderController {

    private final LaundryOrderService laundryOrders;

    LaundryOrderController(LaundryOrderService laundryOrders) {
        this.laundryOrders = laundryOrders;
    }

    @PostMapping
    @Operation(summary = "Receive a confirmed order into the laundry (starts in RECEIVED)")
    ResponseEntity<LaundryOrderResource> receive(@Valid @RequestBody ReceiveLaundryOrderRequest request) {
        LaundryOrder created = laundryOrders.receive(request.orderId(), request.orderNumber(),
                request.customerId(), request.garmentCount());
        return ResponseEntity.created(URI.create("/api/v1/laundryOrders/" + created.id()))
                .body(LaundryOrderResource.from(created));
    }

    @PostMapping("/{id}/advance")
    @Operation(summary = "Advance the laundry order to its next processing stage")
    LaundryOrderResource advance(@PathVariable Long id) {
        return LaundryOrderResource.from(laundryOrders.advance(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a laundry order by id")
    LaundryOrderResource get(@PathVariable Long id) {
        return LaundryOrderResource.from(laundryOrders.getById(id));
    }

    @GetMapping
    @Operation(summary = "List laundry orders, oldest update first; optionally filtered by status or customerId")
    List<LaundryOrderResource> list(@RequestParam(required = false) LaundryStatus status,
                                    @RequestParam(required = false) Long customerId) {
        return laundryOrders.search(status, customerId).stream().map(LaundryOrderResource::from).toList();
    }
}
