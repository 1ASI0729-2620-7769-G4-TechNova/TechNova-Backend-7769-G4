package com.technova.washtrack.billing.domain.model;

import java.time.Instant;
import java.time.ZoneOffset;

import com.technova.washtrack.shared.exception.BusinessRuleException;

/** Aggregate that holds the plan a laundry has contracted and the period it covers. */
public class Subscription {

    private final Long id;
    private final Long laundryId;
    private Instant startDate;
    private Instant endDate;
    private Long planId;
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

    /** Switches an active subscription to another plan, keeping the current billing period. */
    public void changePlan(Plan newPlan) {
        if (!isActive()) {
            throw new BusinessRuleException("Only an active subscription can change plan; renew it first");
        }
        if (newPlan.id().equals(planId)) {
            throw new BusinessRuleException("The subscription is already on this plan");
        }
        planId = newPlan.id();
    }

    /**
     * Renews the subscription for one more billing period of {@code plan}. An active subscription is
     * extended from its current end date; an expired one starts a new period from {@code now}.
     */
    public void renew(Plan plan, Instant now) {
        if (status == SubscriptionStatus.CANCELLED) {
            throw new BusinessRuleException("A cancelled subscription cannot be renewed; subscribe again");
        }
        if (status == SubscriptionStatus.EXPIRED) {
            startDate = now;
            endDate = endOfPeriod(now, plan);
        } else {
            endDate = endOfPeriod(endDate, plan);
        }
        status = SubscriptionStatus.ACTIVE;
    }

    public void cancel() {
        if (!isActive()) {
            throw new BusinessRuleException("Only an active subscription can be cancelled");
        }
        status = SubscriptionStatus.CANCELLED;
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
