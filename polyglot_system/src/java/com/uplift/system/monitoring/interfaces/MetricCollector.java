package com.uplift.system.monitoring.interfaces;

import com.uplift.system.monitoring.models.Metric;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Interface for collecting and managing metrics.
 */
public interface MetricCollector {
    
    /**
     * Records a single metric.
     *
     * @param metric The metric to record
     * @throws com.uplift.system.monitoring.exceptions.MetricCollectionException if collection fails
     */
    void recordMetric(@NotNull Metric metric);

    /**
     * Records multiple metrics in batch.
     *
     * @param metrics The list of metrics to record
     * @throws com.uplift.system.monitoring.exceptions.MetricCollectionException if collection fails
     */
    void recordMetrics(@NotNull List<Metric> metrics);

    /**
     * Retrieves metrics for a specific domain.
     *
     * @param domain The domain to get metrics for
     * @return List of metrics for the domain
     */
    List<Metric> getMetricsForDomain(@NotNull String domain);

    /**
     * Retrieves all metrics.
     *
     * @return List of all recorded metrics
     */
    List<Metric> getAllMetrics();

    /**
     * Clears all recorded metrics.
     */
    void clearMetrics();
} 