package com.technova.washtrack.tracking.interfaces.rest;

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

import com.technova.washtrack.tracking.application.TrackingService;
import com.technova.washtrack.tracking.domain.model.OrderTracking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Paths keep the names the frontend already uses (orderTrackings, stageChanges). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Tracking", description = "Current stage and stage history of orders")
public class TrackingController {

    private final TrackingService tracking;

    TrackingController(TrackingService tracking) {
        this.tracking = tracking;
    }

    @PostMapping("/orderTrackings")
    @Operation(summary = "Start tracking an order (stage RECEIVED)")
    ResponseEntity<OrderTrackingResource> start(@Valid @RequestBody StartTrackingRequest request) {
        OrderTracking started = tracking.start(request.orderId(), request.orderNumber(), request.customerId());
        return ResponseEntity.created(URI.create("/api/v1/orderTrackings/" + started.id()))
                .body(OrderTrackingResource.from(started));
    }

    @PostMapping("/orderTrackings/{id}/advance")
    @Operation(summary = "Move the order to its next stage (no skipping, nothing after DELIVERED)")
    OrderTrackingResource advance(@PathVariable Long id) {
        return OrderTrackingResource.from(tracking.advance(id));
    }

    @GetMapping("/orderTrackings/{id}")
    @Operation(summary = "Get the tracking of an order by id")
    OrderTrackingResource get(@PathVariable Long id) {
        return OrderTrackingResource.from(tracking.getById(id));
    }

    @GetMapping("/orderTrackings")
    @Operation(summary = "List trackings, most recently updated first; optionally of one customer")
    List<OrderTrackingResource> list(@RequestParam(required = false) Long customerId) {
        return tracking.search(customerId).stream().map(OrderTrackingResource::from).toList();
    }

    @GetMapping("/stageChanges")
    @Operation(summary = "Stage history of an order, oldest first")
    List<StageChangeResource> history(@RequestParam Long orderId) {
        return tracking.history(orderId).stream().map(StageChangeResource::from).toList();
    }
}
