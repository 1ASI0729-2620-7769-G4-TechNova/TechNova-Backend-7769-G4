package com.technova.washtrack.billing.interfaces.rest;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.billing.application.PaymentService;
import com.technova.washtrack.billing.domain.model.Payment;
import com.technova.washtrack.billing.domain.model.PaymentStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Registration and history of order payments")
public class PaymentController {

    private final PaymentService payments;

    PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping
    @Operation(summary = "Register a payment for an order (starts as PENDING)")
    ResponseEntity<PaymentResource> register(@Valid @RequestBody RegisterPaymentRequest request) {
        Payment payment = payments.register(request.orderId(), request.customerName(), request.amount(),
                request.paymentMethod());
        return ResponseEntity.created(URI.create("/api/v1/payments/" + payment.id()))
                .body(PaymentResource.from(payment));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending payment (e.g. cash received at the counter)")
    PaymentResource confirm(@PathVariable Long id) {
        return PaymentResource.from(payments.confirm(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a pending payment, keeping the reason")
    PaymentResource reject(@PathVariable Long id, @Valid @RequestBody RejectPaymentRequest request) {
        return PaymentResource.from(payments.reject(id, request.reason()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment by id")
    PaymentResource get(@PathVariable Long id) {
        return PaymentResource.from(payments.getById(id));
    }

    @GetMapping
    @Operation(summary = "Payment history, newest first; from/to are inclusive UTC dates")
    List<PaymentResource> history(@RequestParam(required = false) Long orderId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return payments.search(orderId, status,
                from == null ? null : from.atStartOfDay().toInstant(ZoneOffset.UTC),
                to == null ? null : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                .stream().map(PaymentResource::from).toList();
    }
}
