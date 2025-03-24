package com.uplift.system.events;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Unified event bus interface for cross-language communication
 */
public interface EventBus {
    /**
     * Publish an event to all subscribers
     * @param eventType The type of event
     * @param payload Event data
     */
    void publish(String eventType, Map<String, Object> payload);

    /**
     * Subscribe to events of a specific type
     * @param eventType The type of event to subscribe to
     * @param listener The listener to be notified
     */
    void subscribe(String eventType, Consumer<Map<String, Object>> listener);

    /**
     * Unsubscribe from events of a specific type
     * @param eventType The type of event
     * @param listener The listener to remove
     */
    void unsubscribe(String eventType, Consumer<Map<String, Object>> listener);

    /**
     * Get metrics about event processing
     * @return Map of metric name to value
     */
    Map<String, Object> getMetrics();
} 