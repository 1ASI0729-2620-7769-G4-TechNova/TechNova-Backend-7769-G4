package com.technova.washtrack.billing.domain.model;

import java.time.Instant;
import java.time.ZoneOffset;

/** Aggregate that holds the plan a laundry has contracted and the period it covers. */
public class Subscription {

    private final Long id;
    private final Long laundryId;
    private final Long planId;
    private final Instant startDate;
    private final Instant endDate;
    private SubscriptionStatus status;

    private Subscription(Long id, Long laundryId, Long planId, Instant startDate, Instant endDate) {
        this.id = id;
        this.laundryId = laundryId;
        this.planId = planId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = SubscriptionStatus.ACTIVE;
    }

    /** Starts an active subscription to {@code plan} covering one billing period from {@code now}. */
    public static Subscription subscribe(Long id, Long laundryId, Plan plan, Instant now) {
        return new Subscription(id, laundryId, plan.id(), now, endOfPeriod(now, plan));
    }

    /** Moves an active subscription to {@code EXPIRED} once its end date has passed. */
    public void refresh(Instant now) {
        if (status == SubscriptionStatus.ACTIVE && !now.isBefore(endDate)) {
            status = SubscriptionStatus.EXPIRED;
        }
    }

    private static Instant endOfPeriod(Instant from, Plan plan) {
        return from.atZone(ZoneOffset.UTC).plusMonths(plan.billingMonths()).toInstant();
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public Long id() {
        return id;
    }

    public Long laundryId() {
        return laundryId;
    }

    public Long planId() {
        return planId;
    }

    public Instant startDate() {
        return startDate;
    }

    public Instant endDate() {
        return endDate;
    }

    public SubscriptionStatus status() {
        return status;
    }
}
