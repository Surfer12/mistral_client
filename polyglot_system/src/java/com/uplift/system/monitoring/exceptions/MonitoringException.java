package com.uplift.system.monitoring.exceptions;

/**
 * Base exception class for all monitoring-related errors.
 */
public class MonitoringException extends RuntimeException {
    
    public MonitoringException(String message) {
        super(message);
    }

    public MonitoringException(String message, Throwable cause) {
        super(message, cause);
    }
} 