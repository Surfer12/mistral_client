package com.uplift.system.monitoring.models;

import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single metric measurement with metadata.
 */
public class Metric {
    private final String name;
    private final Object value;
    private final MetricType type;
    private final Instant timestamp;
    private final Map<String, String> tags;
    private final String domain;

    /**
     * Metric types supported by the monitoring system.
     */
    public enum MetricType {
        COUNTER,
        GAUGE,
        HISTOGRAM,
        SUMMARY,
        TIMER
    }

    private Metric(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Metric name must not be null");
        this.value = Objects.requireNonNull(builder.value, "Metric value must not be null");
        this.type = Objects.requireNonNull(builder.type, "Metric type must not be null");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp must not be null");
        this.tags = new HashMap<>(builder.tags);
        this.domain = builder.domain;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Object getValue() {
        return value;
    }

    @NotNull
    public MetricType getType() {
        return type;
    }

    @NotNull
    public Instant getTimestamp() {
        return timestamp;
    }

    @NotNull
    public Map<String, String> getTags() {
        return new HashMap<>(tags);
    }

    public String getDomain() {
        return domain;
    }

    /**
     * Builder for creating Metric instances.
     */
    public static class Builder {
        private String name;
        private Object value;
        private MetricType type;
        private Instant timestamp = Instant.now();
        private final Map<String, String> tags = new HashMap<>();
        private String domain;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public Builder type(MetricType type) {
            this.type = type;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder addTag(String key, String value) {
            this.tags.put(key, value);
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Metric build() {
            return new Metric(this);
        }
    }
} 