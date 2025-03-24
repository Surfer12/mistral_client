package com.uplift.system.adapters;

import java.util.Map;

/**
 * Base interface for language-specific adapters
 */
public interface LanguageAdapter {
    /**
     * Convert an entity from its native format to a normalized form
     * @param nativeEntity The entity in its native language format
     * @return Normalized entity representation
     */
    Map<String, Object> toNormalizedForm(Object nativeEntity);

    /**
     * Convert a normalized entity back to its native format
     * @param normalizedForm The normalized entity representation
     * @return Entity in native format
     */
    Object fromNormalizedForm(Map<String, Object> normalizedForm);

    /**
     * Validate that an entity meets the requirements for this language
     * @param entity The entity to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validate(Object entity);

    /**
     * Get the language identifier for this adapter
     * @return Language identifier (e.g., "java", "python", etc.)
     */
    String getLanguageIdentifier();

    /**
     * Get adapter-specific metrics
     * @return Map of metric name to value
     */
    Map<String, Object> getMetrics();
} 