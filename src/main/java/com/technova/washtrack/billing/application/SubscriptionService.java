package com.technova.washtrack.billing.application;

import java.time.Clock;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.technova.washtrack.billing.domain.event.SubscriptionPlanChanged;
import com.technova.washtrack.billing.domain.model.Plan;
import com.technova.washtrack.billing.domain.model.Subscription;
import com.technova.washtrack.billing.domain.repository.SubscriptionRepository;
import com.technova.washtrack.shared.exception.BusinessRuleException;
import com.technova.washtrack.shared.exception.ResourceNotFoundException;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptions;
    private final PlanQueryService plans;
    private final Clock clock;
    private final ApplicationEventPublisher events;

    SubscriptionService(SubscriptionRepository subscriptions, PlanQueryService plans, Clock clock,
            ApplicationEventPublisher events) {
        this.subscriptions = subscriptions;
        this.plans = plans;
        this.clock = clock;
        this.events = events;
    }

    /** Subscribes a laundry to a plan; a laundry can hold only one active subscription at a time. */
    public Subscription subscribe(Long laundryId, Long planId) {
        Plan plan = plans.getById(planId);
        if (findByLaundry(laundryId).stream().anyMatch(Subscription::isActive)) {
            throw new BusinessRuleException("Laundry " + laundryId + " already has an active subscription");
        }
        return subscriptions.save(Subscription.subscribe(subscriptions.nextId(), laundryId, plan, clock.instant()));
    }

    public Subscription changePlan(Long id, Long planId) {
        Subscription subscription = getById(id);
        Long oldPlanId = subscription.planId();
        subscription.changePlan(plans.getById(planId));
        subscriptions.save(subscription);
        events.publishEvent(new SubscriptionPlanChanged(id, subscription.laundryId(), oldPlanId, planId));
        return subscription;
    }

    public Subscription getById(Long id) {
        Subscription subscription = subscriptions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", id));
        return refreshed(subscription);
    }

    /** Subscriptions of a laundry, newest first, with expired ones already marked. */
    public List<Subscription> findByLaundry(Long laundryId) {
        return subscriptions.findByLaundryId(laundryId).stream().map(this::refreshed).toList();
    }

    private Subscription refreshed(Subscription subscription) {
        subscription.refresh(clock.instant());
        return subscriptions.save(subscription);
    }
}
