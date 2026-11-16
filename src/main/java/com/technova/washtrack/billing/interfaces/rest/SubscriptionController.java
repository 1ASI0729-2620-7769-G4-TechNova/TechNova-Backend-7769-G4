package com.technova.washtrack.billing.interfaces.rest;

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

import com.technova.washtrack.billing.application.SubscriptionService;
import com.technova.washtrack.billing.domain.model.Subscription;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "Plans contracted by laundries")
public class SubscriptionController {

    private final SubscriptionService subscriptions;

    SubscriptionController(SubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    @PostMapping
    @Operation(summary = "Subscribe a laundry to a plan")
    ResponseEntity<SubscriptionResource> subscribe(@Valid @RequestBody SubscribeRequest request) {
        Subscription subscription = subscriptions.subscribe(request.laundryId(), request.planId());
        return ResponseEntity.created(URI.create("/api/v1/subscriptions/" + subscription.id()))
                .body(SubscriptionResource.from(subscription));
    }

    @PostMapping("/{id}/change-plan")
    @Operation(summary = "Move an active subscription to another plan")
    SubscriptionResource changePlan(@PathVariable Long id, @Valid @RequestBody ChangePlanRequest request) {
        return SubscriptionResource.from(subscriptions.changePlan(id, request.planId()));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew for one more billing period (also reactivates an expired subscription)")
    SubscriptionResource renew(@PathVariable Long id) {
        return SubscriptionResource.from(subscriptions.renew(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an active subscription")
    SubscriptionResource cancel(@PathVariable Long id) {
        return SubscriptionResource.from(subscriptions.cancel(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a subscription by id; an overdue one is reported as EXPIRED")
    SubscriptionResource get(@PathVariable Long id) {
        return SubscriptionResource.from(subscriptions.getById(id));
    }

    @GetMapping
    @Operation(summary = "Subscriptions of a laundry, newest first")
    List<SubscriptionResource> byLaundry(@RequestParam Long laundryId) {
        return subscriptions.findByLaundry(laundryId).stream().map(SubscriptionResource::from).toList();
    }
}
