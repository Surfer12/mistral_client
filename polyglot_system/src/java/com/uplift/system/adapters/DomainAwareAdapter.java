package com.uplift.system.adapters;

import com.uplift.system.events.DomainAwareEventBus.Domain;
import java.util.Map;
import java.util.Set;

/**
 * Extended adapter interface that is aware of different domains
 * and can handle cross-domain transformations
 */
public interface DomainAwareAdapter extends LanguageAdapter {
    /**
     * Get the domains this adapter operates in
     * @return Set of domains this adapter supports
     */
    Set<Domain> getSupportedDomains();

    /**
     * Transform an entity between domains
     * @param entity The entity to transform
     * @param sourceDomain Source domain
     * @param targetDomain Target domain
     * @return Transformed entity
     */
    Object transformBetweenDomains(Object entity, Domain sourceDomain, Domain targetDomain);

    /**
     * Check if this adapter can handle a specific domain transformation
     * @param sourceDomain Source domain
     * @param targetDomain Target domain
     * @return true if the transformation is supported
     */
    boolean supportsTransformation(Domain sourceDomain, Domain targetDomain);

    /**
     * Get domain-specific metrics for this adapter
     * @param domain The domain to get metrics for
     * @return Map of metric name to value
     */
    Map<String, Object> getDomainMetrics(Domain domain);

    /**
     * Apply domain-specific validation rules
     * @param entity The entity to validate
     * @param domain The domain to validate against
     * @throws IllegalArgumentException if validation fails
     */
    void validateForDomain(Object entity, Domain domain);

    /**
     * Get the optimization strategy for a specific domain
     * @param domain The domain to get optimization strategy for
     * @return Map of optimization parameters
     */
    Map<String, Object> getDomainOptimizationStrategy(Domain domain);
} 