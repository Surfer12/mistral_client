package com.uplift.system.config;

import com.uplift.system.events.DomainAwareEventBus.Domain;
import java.util.Map;
import java.util.HashMap;
import java.time.Duration;
import java.util.EnumMap;

/**
 * Enhanced system-wide configuration settings with domain awareness.
 * Provides centralized configuration management with support for both global and domain-specific settings.
 * 
 * <p>The configuration system supports:
 * <ul>
 *   <li>Global settings for system-wide features</li>
 *   <li>Domain-specific settings for specialized behavior</li>
 *   <li>Default values for common configuration options</li>
 *   <li>Dynamic configuration updates at runtime</li>
 * </ul>
 * 
 * <p>Settings are organized hierarchically with dot notation (e.g. "cache.enabled", "performance.optimizationInterval").
 * Domain-specific settings are maintained separately from global settings.
 */
public class SystemConfig {
    private final Map<String, Object> settings;
    private final Map<Domain, Map<String, Object>> domainSettings;
    
    /**
     * Constructs a new SystemConfig instance with default settings.
     * Initializes both global and domain-specific configuration maps and populates default values.
     */
    public SystemConfig() {
        this.settings = new HashMap<>();
        this.domainSettings = new EnumMap<>(Domain.class);
        setDefaults();
    }
    
    /**
     * Initializes default configuration values for both global and domain-specific settings.
     * 
     * <p>Global defaults include:
     * <ul>
     *   <li>Cache configuration (enabled, size, expiration)</li>
     *   <li>Performance settings (optimization interval, concurrent operations)</li>
     *   <li>Monitoring configuration (enabled, metrics interval)</li>
     *   <li>Event bus settings (queue size, worker threads)</li>
     * </ul>
     * 
     * <p>Domain-specific defaults are set based on the characteristics of each domain:
     * <ul>
     *   <li>COMPUTATIONAL: Performance-focused settings</li>
     *   <li>COGNITIVE: Adaptive learning settings</li>
     *   <li>REPRESENTATIONAL: Space optimization settings</li>
     * </ul>
     */
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
    
    /**
     * Sets a global configuration value.
     * 
     * @param key The configuration key using dot notation
     * @param value The value to set
     */
    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }
    
    /**
     * Retrieves a global configuration value.
     * 
     * @param key The configuration key using dot notation
     * @return The configuration value, or null if not found
     */
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    /**
     * Retrieves a global configuration value with a default fallback.
     * 
     * @param key The configuration key using dot notation
     * @param defaultValue The default value to return if the key is not found
     * @return The configuration value, or defaultValue if not found
     */
    public Object getSetting(String key, Object defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }
    
    /**
     * Checks if a feature is enabled in the global configuration.
     * 
     * @param feature The feature name (without the ".enabled" suffix)
     * @return true if the feature is enabled, false otherwise
     */
    public boolean isEnabled(String feature) {
        return (boolean) settings.getOrDefault(feature + ".enabled", false);
    }
    
    /**
     * Retrieves all configuration settings, including both global and domain-specific settings.
     * 
     * @return A map containing all settings, with domain settings nested under the "domains" key
     */
    public Map<String, Object> getAllSettings() {
        Map<String, Object> allSettings = new HashMap<>(settings);
        allSettings.put("domains", domainSettings);
        return allSettings;
    }
    
    /**
     * Loads multiple configuration settings from a map.
     * 
     * @param newSettings Map of configuration keys and values to load
     */
    public void loadFromMap(Map<String, Object> newSettings) {
        settings.putAll(newSettings);
    }

    /**
     * Sets a domain-specific configuration value.
     * 
     * @param domain The domain to configure
     * @param key The configuration key using dot notation
     * @param value The value to set
     */
    public void setDomainSetting(Domain domain, String key, Object value) {
        domainSettings.computeIfAbsent(domain, k -> new HashMap<>()).put(key, value);
    }
    
    /**
     * Retrieves a domain-specific configuration value.
     * 
     * @param domain The domain to query
     * @param key The configuration key using dot notation
     * @return The configuration value, or null if not found
     */
    public Object getDomainSetting(Domain domain, String key) {
        Map<String, Object> domainConfig = domainSettings.get(domain);
        return domainConfig != null ? domainConfig.get(key) : null;
    }
    
    /**
     * Retrieves a domain-specific configuration value with a default fallback.
     * 
     * @param domain The domain to query
     * @param key The configuration key using dot notation
     * @param defaultValue The default value to return if the key is not found
     * @return The configuration value, or defaultValue if not found
     */
    public Object getDomainSetting(Domain domain, String key, Object defaultValue) {
        Object value = getDomainSetting(domain, key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Retrieves all configuration settings for a specific domain.
     * 
     * @param domain The domain to query
     * @return A map containing all settings for the specified domain
     */
    public Map<String, Object> getDomainSettings(Domain domain) {
        return new HashMap<>(domainSettings.getOrDefault(domain, new HashMap<>()));
    }
    
    /**
     * Checks if a feature is enabled for a specific domain.
     * 
     * @param domain The domain to query
     * @param feature The feature name (without the ".enabled" suffix)
     * @return true if the feature is enabled for the domain, false otherwise
     */
    public boolean isDomainFeatureEnabled(Domain domain, String feature) {
        Object value = getDomainSetting(domain, feature + ".enabled");
        return value != null ? (boolean) value : false;
    }
} 