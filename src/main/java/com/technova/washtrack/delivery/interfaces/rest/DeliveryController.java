package com.technova.washtrack.delivery.interfaces.rest;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.delivery.application.DeliveryService;
import com.technova.washtrack.delivery.domain.model.Delivery;
import com.technova.washtrack.delivery.domain.model.DeliveryStatus;
import com.technova.washtrack.delivery.domain.model.DeliveryType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/deliveries")
@Tag(name = "Deliveries", description = "Pickups and home deliveries of laundry orders")
public class DeliveryController {

    private final DeliveryService deliveries;

    DeliveryController(DeliveryService deliveries) {
        this.deliveries = deliveries;
    }

    @PostMapping
    @Operation(summary = "Schedule a pickup or delivery (409 when the time slot is full)")
    ResponseEntity<DeliveryResource> schedule(@Valid @RequestBody ScheduleDeliveryRequest request) {
        Delivery delivery = deliveries.schedule(request.orderId(), request.customerName(), request.type(),
                request.address(), request.scheduledAt());
        return ResponseEntity.created(URI.create("/api/v1/deliveries/" + delivery.id()))
                .body(DeliveryResource.from(delivery));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Assign an available driver to a scheduled delivery")
    DeliveryResource assignDriver(@PathVariable Long id, @Valid @RequestBody AssignDriverRequest request) {
        return DeliveryResource.from(deliveries.assignDriver(id, request.driverId()));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "The driver leaves for the address (SCHEDULED -> ON_THE_WAY)")
    DeliveryResource start(@PathVariable Long id) {
        return DeliveryResource.from(deliveries.start(id));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark the delivery as completed (ON_THE_WAY -> COMPLETED)")
    DeliveryResource complete(@PathVariable Long id) {
        return DeliveryResource.from(deliveries.complete(id));
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Mark an active delivery as failed, with a reason")
    DeliveryResource fail(@PathVariable Long id, @Valid @RequestBody FailDeliveryRequest request) {
        return DeliveryResource.from(deliveries.fail(id, request.reason()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a delivery by id")
    DeliveryResource get(@PathVariable Long id) {
        return DeliveryResource.from(deliveries.getById(id));
    }

    @GetMapping
    @Operation(summary = "List deliveries by scheduled time; from/to are inclusive UTC dates")
    List<DeliveryResource> list(@RequestParam(required = false) Long orderId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) DeliveryType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return deliveries.search(orderId, status, type,
                from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                .stream().map(DeliveryResource::from).toList();
    }
}
