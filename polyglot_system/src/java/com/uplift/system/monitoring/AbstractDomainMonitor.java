package com.uplift.system.monitoring;

import com.uplift.system.monitoring.interfaces.MetricCollector;
import com.uplift.system.monitoring.models.Metric;
import com.uplift.system.monitoring.exceptions.MetricCollectionException;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abstract base class for domain monitoring implementations.
 * Provides common functionality for metric collection and event handling.
 */
public abstract class AbstractDomainMonitor implements MetricCollector {
    
    protected final Map<String, List<Metric>> metricsByDomain;
    protected final List<Consumer<Metric>> metricListeners;
    protected final String monitorName;

    protected AbstractDomainMonitor(String monitorName) {
        this.monitorName = monitorName;
        this.metricsByDomain = new ConcurrentHashMap<>();
        this.metricListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void recordMetric(@NotNull Metric metric) {
        try {
            validateMetric(metric);
            storeMetric(metric);
            notifyListeners(metric);
        } catch (Exception e) {
            throw new MetricCollectionException("Failed to record metric: " + metric.getName(), e);
        }
    }

    @Override
    public void recordMetrics(@NotNull List<Metric> metrics) {
        List<Exception> errors = new ArrayList<>();
        
        for (Metric metric : metrics) {
            try {
                recordMetric(metric);
            } catch (Exception e) {
                errors.add(e);
            }
        }
        
        if (!errors.isEmpty()) {
            throw new MetricCollectionException(
                String.format("Failed to record %d out of %d metrics", errors.size(), metrics.size()),
                errors.get(0)
            );
        }
    }

    @Override
    public List<Metric> getMetricsForDomain(@NotNull String domain) {
        return new ArrayList<>(metricsByDomain.getOrDefault(domain, new ArrayList<>()));
    }

    @Override
    public List<Metric> getAllMetrics() {
        List<Metric> allMetrics = new ArrayList<>();
        metricsByDomain.values().forEach(allMetrics::addAll);
        return allMetrics;
    }

    @Override
    public void clearMetrics() {
        metricsByDomain.clear();
    }

    /**
     * Adds a listener to be notified when new metrics are recorded.
     *
     * @param listener The listener to add
     */
    public void addMetricListener(Consumer<Metric> listener) {
        metricListeners.add(listener);
    }

    /**
     * Removes a previously added metric listener.
     *
     * @param listener The listener to remove
     */
    public void removeMetricListener(Consumer<Metric> listener) {
        metricListeners.remove(listener);
    }

    /**
     * Validates a metric before recording.
     * 
     * @param metric The metric to validate
     * @throws MetricCollectionException if validation fails
     */
    protected abstract void validateMetric(@NotNull Metric metric);

    /**
     * Performs any necessary preprocessing on the metric before storage.
     * 
     * @param metric The metric to preprocess
     * @return The preprocessed metric
     */
    protected abstract Metric preprocessMetric(@NotNull Metric metric);

    private void storeMetric(@NotNull Metric metric) {
        Metric processedMetric = preprocessMetric(metric);
        String domain = processedMetric.getDomain();
        
        metricsByDomain.computeIfAbsent(domain, k -> new CopyOnWriteArrayList<>())
                      .add(processedMetric);
    }

    private void notifyListeners(@NotNull Metric metric) {
        for (Consumer<Metric> listener : metricListeners) {
            try {
                listener.accept(metric);
            } catch (Exception e) {
                // Log error but continue notifying other listeners
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
} 