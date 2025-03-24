package com.uplift.system.config;

import com.uplift.system.events.DomainAwareEventBus.Domain;
import java.util.Map;
import java.util.HashMap;
import java.time.Duration;
import java.util.EnumMap;

/**
 * Enhanced system-wide configuration settings with domain awareness
 */
public class SystemConfig {
    private final Map<String, Object> settings;
    private final Map<Domain, Map<String, Object>> domainSettings;
    
    public SystemConfig() {
        this.settings = new HashMap<>();
        this.domainSettings = new EnumMap<>(Domain.class);
        setDefaults();
    }
    
    private void setDefaults() {
        // Global settings
        settings.put("cache.enabled", true);
        settings.put("cache.maxSize", 10000);
        settings.put("cache.expiration", Duration.ofMinutes(30));
        
        settings.put("performance.optimizationInterval", Duration.ofMinutes(60));
        settings.put("performance.maxConcurrentOperations", 100);
        
        settings.put("monitoring.enabled", true);
        settings.put("monitoring.metricsInterval", Duration.ofSeconds(30));
        
        settings.put("eventBus.maxQueueSize", 1000);
        settings.put("eventBus.workerThreads", 4);

        // Domain-specific settings
        for (Domain domain : Domain.values()) {
            Map<String, Object> domainConfig = new HashMap<>();
            
            switch (domain) {
                case COMPUTATIONAL:
                    domainConfig.put("optimization.strategy", "performance");
                    domainConfig.put("cache.policy", "lru");
                    domainConfig.put("validation.level", "strict");
                    break;
                    
                case COGNITIVE:
                    domainConfig.put("optimization.strategy", "adaptive");
                    domainConfig.put("learning.enabled", true);
                    domainConfig.put("feedback.interval", Duration.ofMinutes(5));
                    break;
                    
                case REPRESENTATIONAL:
                    domainConfig.put("optimization.strategy", "space");
                    domainConfig.put("compression.enabled", true);
                    domainConfig.put("indexing.enabled", true);
                    break;
            }
            
            domainSettings.put(domain, domainConfig);
        }
    }
    
    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }
    
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    public Object getSetting(String key, Object defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }
    
    public boolean isEnabled(String feature) {
        return (boolean) settings.getOrDefault(feature + ".enabled", false);
    }
    
    public Map<String, Object> getAllSettings() {
        Map<String, Object> allSettings = new HashMap<>(settings);
        allSettings.put("domains", domainSettings);
        return allSettings;
    }
    
    public void loadFromMap(Map<String, Object> newSettings) {
        settings.putAll(newSettings);
    }

    // Domain-specific methods
    public void setDomainSetting(Domain domain, String key, Object value) {
        domainSettings.computeIfAbsent(domain, k -> new HashMap<>()).put(key, value);
    }
    
    public Object getDomainSetting(Domain domain, String key) {
        Map<String, Object> domainConfig = domainSettings.get(domain);
        return domainConfig != null ? domainConfig.get(key) : null;
    }
    
    public Object getDomainSetting(Domain domain, String key, Object defaultValue) {
        Object value = getDomainSetting(domain, key);
        return value != null ? value : defaultValue;
    }
    
    public Map<String, Object> getDomainSettings(Domain domain) {
        return new HashMap<>(domainSettings.getOrDefault(domain, new HashMap<>()));
    }
    
    public boolean isDomainFeatureEnabled(Domain domain, String feature) {
        Object value = getDomainSetting(domain, feature + ".enabled");
        return value != null ? (boolean) value : false;
    }
} 