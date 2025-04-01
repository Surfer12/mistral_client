package com.uplift.system.events;

import com.uplift.system.config.SystemConfig;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Domain-aware implementation of the EventBus interface that supports event routing
 * and transformation between different cognitive, computational, and representational domains.
 */
public class DomainAwareEventBus implements EventBus {
    
    /**
     * Represents the different domains that events can be processed in
     */
    public enum Domain {
        COGNITIVE,
        COMPUTATIONAL,
        REPRESENTATIONAL
    }

    private final SystemConfig config;
    private final Map<String, List<Consumer<Map<String, Object>>>> subscribers;
    private final Map<String, Domain> eventDomains;
    private final Map<String, AtomicLong> metrics;
    private final Map<Domain, List<DomainTransformer>> domainTransformers;

    /**
     * Constructs a new DomainAwareEventBus with the specified configuration.
     *
     * @param config The system configuration
     * @throws IllegalArgumentException if config is null
     */
    public DomainAwareEventBus(@NotNull SystemConfig config) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.subscribers = new ConcurrentHashMap<>();
        this.eventDomains = new ConcurrentHashMap<>();
        this.metrics = new ConcurrentHashMap<>();
        this.domainTransformers = new ConcurrentHashMap<>();
        initializeMetrics();
        initializeDomainTransformers();
    }

    private void initializeMetrics() {
        metrics.put("totalEvents", new AtomicLong(0));
        metrics.put("domainTransformations", new AtomicLong(0));
        metrics.put("activeSubscribers", new AtomicLong(0));
        metrics.put("eventLatency", new AtomicLong(0));
        
        // Domain-specific metrics
        for (Domain domain : Domain.values()) {
            metrics.put("events." + domain.name().toLowerCase(), new AtomicLong(0));
        }
    }

    private void initializeDomainTransformers() {
        for (Domain domain : Domain.values()) {
            domainTransformers.put(domain, new CopyOnWriteArrayList<>());
        }
    }

    /**
     * Registers a domain transformer for a specific domain.
     *
     * @param domain The domain to register the transformer for
     * @param transformer The transformer to register
     */
    public void registerDomainTransformer(@NotNull Domain domain, @NotNull DomainTransformer transformer) {
        Objects.requireNonNull(domain, "Domain must not be null");
        Objects.requireNonNull(transformer, "Transformer must not be null");
        domainTransformers.get(domain).add(transformer);
    }

    @Override
    public void publish(@NotNull String eventType, @NotNull Map<String, Object> payload) {
        Objects.requireNonNull(eventType, "Event type must not be null");
        Objects.requireNonNull(payload, "Payload must not be null");
        
        long startTime = System.nanoTime();
        metrics.get("totalEvents").incrementAndGet();

        // Determine event domain
        Domain eventDomain = determineEventDomain(eventType, payload);
        metrics.get("events." + eventDomain.name().toLowerCase()).incrementAndGet();

        // Transform payload for each subscriber's domain
        List<Consumer<Map<String, Object>>> eventSubscribers = subscribers.getOrDefault(eventType, Collections.emptyList());
        for (Consumer<Map<String, Object>> subscriber : eventSubscribers) {
            Domain targetDomain = getSubscriberDomain(subscriber);
            Map<String, Object> transformedPayload = transformPayload(payload, eventDomain, targetDomain);
            subscriber.accept(transformedPayload);
        }

        // Update latency metrics
        long endTime = System.nanoTime();
        updateLatencyMetrics(startTime, endTime);
    }

    @Override
    public void subscribe(@NotNull String eventType, @NotNull Consumer<Map<String, Object>> listener) {
        Objects.requireNonNull(eventType, "Event type must not be null");
        Objects.requireNonNull(listener, "Listener must not be null");
        
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        metrics.get("activeSubscribers").incrementAndGet();
    }

    @Override
    public void unsubscribe(@NotNull String eventType, @NotNull Consumer<Map<String, Object>> listener) {
        Objects.requireNonNull(eventType, "Event type must not be null");
        Objects.requireNonNull(listener, "Listener must not be null");
        
        List<Consumer<Map<String, Object>>> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers != null && eventSubscribers.remove(listener)) {
            metrics.get("activeSubscribers").decrementAndGet();
        }
    }

    @Override
    @NotNull
    public Map<String, Object> getMetrics() {
        Map<String, Object> currentMetrics = new HashMap<>();
        metrics.forEach((key, value) -> currentMetrics.put(key, value.get()));
        return currentMetrics;
    }

    /**
     * Determines the domain of an event based on its type and payload.
     */
    private Domain determineEventDomain(@NotNull String eventType, @NotNull Map<String, Object> payload) {
        // Check if domain is explicitly specified in payload
        if (payload.containsKey("domain")) {
            try {
                return Domain.valueOf(payload.get("domain").toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid domain specified, fall through to heuristic determination
            }
        }

        // Use event type prefix if present
        if (eventType.contains(".")) {
            String prefix = eventType.substring(0, eventType.indexOf(".")).toUpperCase();
            try {
                return Domain.valueOf(prefix);
            } catch (IllegalArgumentException e) {
                // Invalid prefix, fall through to heuristic determination
            }
        }

        // Heuristic determination based on payload structure
        if (payload.containsKey("cognitive_state") || payload.containsKey("awareness")) {
            return Domain.COGNITIVE;
        } else if (payload.containsKey("structure") || payload.containsKey("anchors")) {
            return Domain.REPRESENTATIONAL;
        } else {
            return Domain.COMPUTATIONAL; // Default domain
        }
    }

    /**
     * Gets the target domain for a subscriber.
     */
    private Domain getSubscriberDomain(@NotNull Consumer<Map<String, Object>> subscriber) {
        // Implementation could be enhanced to determine subscriber's domain
        // through reflection or registration metadata
        return Domain.COMPUTATIONAL; // Default domain for now
    }

    /**
     * Transforms the payload between domains using registered transformers.
     */
    private Map<String, Object> transformPayload(
            @NotNull Map<String, Object> payload,
            @NotNull Domain sourceDomain,
            @NotNull Domain targetDomain) {
        if (sourceDomain == targetDomain) {
            return new HashMap<>(payload);
        }

        metrics.get("domainTransformations").incrementAndGet();

        // Apply transformers in sequence
        Map<String, Object> transformedPayload = new HashMap<>(payload);
        List<DomainTransformer> transformers = domainTransformers.get(targetDomain);
        for (DomainTransformer transformer : transformers) {
            transformedPayload = transformer.transform(transformedPayload, sourceDomain, targetDomain);
        }

        return transformedPayload;
    }

    private void updateLatencyMetrics(long startTime, long endTime) {
        long currentLatency = metrics.get("eventLatency").get();
        long newLatency = (currentLatency + (endTime - startTime)) / 2; // Rolling average
        metrics.get("eventLatency").set(newLatency);
    }

    /**
     * Interface for domain transformation logic
     */
    public interface DomainTransformer {
        /**
         * Transforms a payload between domains.
         *
         * @param payload The payload to transform
         * @param sourceDomain The source domain
         * @param targetDomain The target domain
         * @return The transformed payload
         */
        Map<String, Object> transform(
            Map<String, Object> payload,
            Domain sourceDomain,
            Domain targetDomain
        );
    }
} 