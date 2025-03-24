package com.uplift.system.monitoring;

import com.uplift.system.events.DomainAwareEventBus.Domain;
import com.uplift.system.config.SystemConfig;
import com.uplift.system.adapters.DomainAwareAdapter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Service for monitoring and visualizing domain integration metrics
 */
public class DomainMonitoringService {
    private final SystemConfig config;
    private final Map<String, MetricCollector> metricCollectors;
    private final Map<String, List<MetricSnapshot>> metricHistory;
    private final ScheduledExecutorService scheduler;
    private final Map<Domain, DomainAwareAdapter> domainAdapters;
    private final List<Consumer<Map<String, Object>>> visualizationListeners;
    
    private ScheduledFuture<?> collectionTask;
    private ScheduledFuture<?> visualizationTask;

    public DomainMonitoringService(SystemConfig config) {
        this.config = config;
        this.metricCollectors = new ConcurrentHashMap<>();
        this.metricHistory = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.domainAdapters = new EnumMap<>(Domain.class);
        this.visualizationListeners = new ArrayList<>();
        
        initializeCollectors();
    }

    private void initializeCollectors() {
        // Domain-specific metrics
        for (Domain domain : Domain.values()) {
            String collectorId = domain.toString().toLowerCase() + "_metrics";
            metricCollectors.put(collectorId, new MetricCollector(
                collectorId,
                () -> collectDomainMetrics(domain)
            ));
        }

        // Integration metrics
        metricCollectors.put("integration_metrics", new MetricCollector(
            "integration_metrics",
            this::collectIntegrationMetrics
        ));

        // Isomorphic structure metrics
        metricCollectors.put("isomorphic_metrics", new MetricCollector(
            "isomorphic_metrics",
            this::collectIsomorphicMetrics
        ));

        // System-wide metrics
        metricCollectors.put("system_metrics", new MetricCollector(
            "system_metrics",
            this::collectSystemMetrics
        ));
    }

    public void registerDomainAdapter(Domain domain, DomainAwareAdapter adapter) {
        domainAdapters.put(domain, adapter);
    }

    public void addVisualizationListener(Consumer<Map<String, Object>> listener) {
        visualizationListeners.add(listener);
    }

    public void startMonitoring() {
        long metricsInterval = config.getSetting("monitoring.metricsInterval", 30000L);
        long visualizationInterval = config.getSetting("monitoring.visualizationInterval", 5000L);

        // Start metric collection
        collectionTask = scheduler.scheduleAtFixedRate(
            this::collectMetrics,
            0,
            metricsInterval,
            TimeUnit.MILLISECONDS
        );

        // Start visualization updates
        visualizationTask = scheduler.scheduleAtFixedRate(
            this::updateVisualizations,
            0,
            visualizationInterval,
            TimeUnit.MILLISECONDS
        );
    }

    public void stopMonitoring() {
        if (collectionTask != null) {
            collectionTask.cancel(false);
        }
        if (visualizationTask != null) {
            visualizationTask.cancel(false);
        }
    }

    private void collectMetrics() {
        metricCollectors.values().forEach(collector -> {
            Map<String, Object> metrics = collector.collect();
            MetricSnapshot snapshot = new MetricSnapshot(
                collector.getId(),
                Instant.now(),
                metrics
            );
            metricHistory.computeIfAbsent(collector.getId(), k -> new ArrayList<>())
                        .add(snapshot);
            
            // Keep history size manageable
            int maxHistory = config.getSetting("monitoring.maxHistorySize", 1000);
            List<MetricSnapshot> history = metricHistory.get(collector.getId());
            if (history.size() > maxHistory) {
                history.subList(0, history.size() - maxHistory).clear();
            }
        });
    }

    private void updateVisualizations() {
        Map<String, Object> visualizationData = new HashMap<>();
        
        // Prepare domain metrics visualization
        Map<String, Object> domainMetrics = new HashMap<>();
        domainAdapters.forEach((domain, adapter) -> {
            domainMetrics.put(domain.toString(), adapter.getDomainMetrics(domain));
        });
        visualizationData.put("domains", domainMetrics);
        
        // Prepare integration metrics
        visualizationData.put("integration", collectIntegrationMetrics());
        
        // Prepare isomorphic metrics
        visualizationData.put("isomorphic", collectIsomorphicMetrics());
        
        // Prepare historical trends
        visualizationData.put("trends", calculateMetricTrends());
        
        // Notify listeners
        visualizationListeners.forEach(listener -> listener.accept(visualizationData));
    }

    private Map<String, Object> collectDomainMetrics(Domain domain) {
        DomainAwareAdapter adapter = domainAdapters.get(domain);
        if (adapter == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Object> metrics = new HashMap<>(adapter.getDomainMetrics(domain));
        metrics.put("optimizationStrategy", adapter.getDomainOptimizationStrategy(domain));
        return metrics;
    }

    private Map<String, Object> collectIntegrationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Collect transformation counts and performance metrics
        metrics.put("transformationCount", calculateTransformationCount());
        metrics.put("averageTransformationTime", calculateAverageTransformationTime());
        metrics.put("successRate", calculateTransformationSuccessRate());
        
        return metrics;
    }

    private Map<String, Object> collectIsomorphicMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Collect metrics for each isomorphic structure
        metrics.put("recursion", collectStructureMetrics("recursion"));
        metrics.put("compression", collectStructureMetrics("compression"));
        metrics.put("metaObservation", collectStructureMetrics("meta_observation"));
        
        return metrics;
    }

    private Map<String, Object> collectSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Collect system-wide performance metrics
        metrics.put("memoryUsage", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        metrics.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        metrics.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        
        return metrics;
    }

    private Map<String, Object> collectStructureMetrics(String structureId) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get historical data for this structure
        List<MetricSnapshot> history = metricHistory.getOrDefault(
            "isomorphic_metrics",
            Collections.emptyList()
        );
        
        if (!history.isEmpty()) {
            MetricSnapshot latest = history.get(history.size() - 1);
            @SuppressWarnings("unchecked")
            Map<String, Object> structureMetrics = (Map<String, Object>) latest.getMetrics()
                .getOrDefault(structureId, Collections.emptyMap());
            metrics.putAll(structureMetrics);
        }
        
        return metrics;
    }

    private Map<String, List<Double>> calculateMetricTrends() {
        Map<String, List<Double>> trends = new HashMap<>();
        
        metricHistory.forEach((collectorId, snapshots) -> {
            if (!snapshots.isEmpty()) {
                // Calculate trends for numeric metrics
                MetricSnapshot latest = snapshots.get(snapshots.size() - 1);
                latest.getMetrics().forEach((metricName, value) -> {
                    if (value instanceof Number) {
                        List<Double> trend = calculateTrendForMetric(collectorId, metricName);
                        trends.put(collectorId + "." + metricName, trend);
                    }
                });
            }
        });
        
        return trends;
    }

    private List<Double> calculateTrendForMetric(String collectorId, String metricName) {
        List<MetricSnapshot> snapshots = metricHistory.get(collectorId);
        List<Double> trend = new ArrayList<>();
        
        snapshots.forEach(snapshot -> {
            Object value = snapshot.getMetrics().get(metricName);
            if (value instanceof Number) {
                trend.add(((Number) value).doubleValue());
            }
        });
        
        return trend;
    }

    private long calculateTransformationCount() {
        return metricHistory.values().stream()
            .flatMap(List::stream)
            .filter(snapshot -> snapshot.getId().equals("integration_metrics"))
            .mapToLong(snapshot -> ((Number) snapshot.getMetrics()
                .getOrDefault("transformationCount", 0L)).longValue())
            .sum();
    }

    private double calculateAverageTransformationTime() {
        List<MetricSnapshot> snapshots = metricHistory.getOrDefault(
            "integration_metrics",
            Collections.emptyList()
        );
        
        if (snapshots.isEmpty()) {
            return 0.0;
        }
        
        return snapshots.stream()
            .mapToDouble(snapshot -> ((Number) snapshot.getMetrics()
                .getOrDefault("averageTransformationTime", 0.0)).doubleValue())
            .average()
            .orElse(0.0);
    }

    private double calculateTransformationSuccessRate() {
        List<MetricSnapshot> snapshots = metricHistory.getOrDefault(
            "integration_metrics",
            Collections.emptyList()
        );
        
        if (snapshots.isEmpty()) {
            return 1.0;
        }
        
        long successful = snapshots.stream()
            .mapToLong(snapshot -> ((Number) snapshot.getMetrics()
                .getOrDefault("successfulTransformations", 0L)).longValue())
            .sum();
            
        long total = snapshots.stream()
            .mapToLong(snapshot -> ((Number) snapshot.getMetrics()
                .getOrDefault("totalTransformations", 0L)).longValue())
            .sum();
            
        return total > 0 ? (double) successful / total : 1.0;
    }

    // Inner classes
    private static class MetricCollector {
        private final String id;
        private final MetricSupplier supplier;

        MetricCollector(String id, MetricSupplier supplier) {
            this.id = id;
            this.supplier = supplier;
        }

        String getId() {
            return id;
        }

        Map<String, Object> collect() {
            return supplier.get();
        }
    }

    private static class MetricSnapshot {
        private final String id;
        private final Instant timestamp;
        private final Map<String, Object> metrics;

        MetricSnapshot(String id, Instant timestamp, Map<String, Object> metrics) {
            this.id = id;
            this.timestamp = timestamp;
            this.metrics = new HashMap<>(metrics);
        }

        String getId() {
            return id;
        }

        Instant getTimestamp() {
            return timestamp;
        }

        Map<String, Object> getMetrics() {
            return Collections.unmodifiableMap(metrics);
        }
    }

    @FunctionalInterface
    private interface MetricSupplier {
        Map<String, Object> get();
    }
} 