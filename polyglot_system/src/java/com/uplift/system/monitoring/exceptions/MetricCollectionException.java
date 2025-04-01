package com.uplift.system.monitoring.exceptions;

/**
 * Exception thrown when there are errors collecting metrics.
 */
public class MetricCollectionException extends MonitoringException {
    
    public MetricCollectionException(String message) {
        super(message);
    }

    public MetricCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
} 