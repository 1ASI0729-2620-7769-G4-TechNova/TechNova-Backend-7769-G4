package com.technova.washtrack.billing.interfaces.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.billing.application.PlanQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Plans", description = "Subscription plans available to laundries")
public class PlanController {

    private final PlanQueryService plans;

    PlanController(PlanQueryService plans) {
        this.plans = plans;
    }

    @GetMapping
    @Operation(summary = "List the available plans")
    List<PlanResource> list() {
        return plans.findAll().stream().map(PlanResource::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a plan by id")
    PlanResource get(@PathVariable Long id) {
        return PlanResource.from(plans.getById(id));
    }
}
