package com.uplift.system.adapters;

import com.uplift.system.events.DomainAwareEventBus.Domain;
import com.uplift.system.config.SystemConfig;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

/**
 * Adapter for the Cognitive domain focusing on neural processing and meta-cognitive awareness.
 * This adapter handles transformations between cognitive representations and other domain formats,
 * maintaining working memory state and attention mechanisms.
 */
public class CognitiveDomainAdapter implements DomainAwareAdapter {
    private final SystemConfig config;
    private final Map<String, AtomicLong> metrics;
    private final Map<String, Object> workingMemory;
    private final Map<String, Object> longTermMemory;
    private final double attentionThreshold;

    /**
     * Constructs a new CognitiveDomainAdapter with the specified configuration.
     *
     * @param config The system configuration containing cognitive domain settings
     * @throws IllegalArgumentException if config is null or missing required settings
     */
    public CognitiveDomainAdapter(@NotNull SystemConfig config) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.metrics = new ConcurrentHashMap<>();
        this.workingMemory = new ConcurrentHashMap<>();
        this.longTermMemory = new ConcurrentHashMap<>();
        this.attentionThreshold = Optional.ofNullable(config.getDomainSetting(
            Domain.COGNITIVE, "attention.threshold", 0.75))
            .map(value -> (double) value)
            .orElse(0.75);
        initializeMetrics();
    }

    private void initializeMetrics() {
        metrics.put("workingMemoryAccess", new AtomicLong(0));
        metrics.put("longTermMemoryAccess", new AtomicLong(0));
        metrics.put("attentionalShifts", new AtomicLong(0));
        metrics.put("metaCognitiveEvents", new AtomicLong(0));
    }

    @Override
    public Set<Domain> getSupportedDomains() {
        return EnumSet.of(Domain.COGNITIVE, Domain.COMPUTATIONAL, Domain.REPRESENTATIONAL);
    }

    @Override
    public Object transformBetweenDomains(Object entity, Domain sourceDomain, Domain targetDomain) {
        metrics.get("metaCognitiveEvents").incrementAndGet();
        
        if (sourceDomain == Domain.COGNITIVE) {
            return transformFromCognitive(entity, targetDomain);
        } else if (targetDomain == Domain.COGNITIVE) {
            return transformToCognitive(entity, sourceDomain);
        }
        
        throw new UnsupportedOperationException("Unsupported domain transformation");
    }

    private Object transformToCognitive(Object entity, Domain sourceDomain) {
        Map<String, Object> cognitiveForm = new HashMap<>();
        
        // Create cognitive structure with working memory and attention components
        cognitiveForm.put("workingMemory", createWorkingMemoryRepresentation(entity));
        cognitiveForm.put("attentionalFocus", calculateAttentionalFocus(entity));
        cognitiveForm.put("metaCognitiveState", createMetaCognitiveState(entity));
        
        // Store in working memory if attention threshold is met
        if (meetAttentionThreshold(cognitiveForm)) {
            String memoryKey = generateMemoryKey(entity);
            workingMemory.put(memoryKey, cognitiveForm);
            metrics.get("workingMemoryAccess").incrementAndGet();
        }
        
        return cognitiveForm;
    }

    private Object transformFromCognitive(Object entity, Domain targetDomain) {
        if (!(entity instanceof Map)) {
            throw new IllegalArgumentException("Cognitive entity must be a Map");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cognitiveEntity = (Map<String, Object>) entity;
        
        // Access working memory and update metrics
        metrics.get("workingMemoryAccess").incrementAndGet();
        
        if (targetDomain == Domain.COMPUTATIONAL) {
            return transformToComputational(cognitiveEntity);
        } else if (targetDomain == Domain.REPRESENTATIONAL) {
            return transformToRepresentational(cognitiveEntity);
        }
        
        throw new UnsupportedOperationException("Unsupported target domain: " + targetDomain);
    }

    private Object transformToComputational(Map<String, Object> cognitiveEntity) {
        Map<String, Object> computationalForm = new HashMap<>();
        
        // Extract working memory contents
        if (cognitiveEntity.containsKey("workingMemory")) {
            computationalForm.put("activeNodes", extractActiveNodes(cognitiveEntity));
            computationalForm.put("processingState", extractProcessingState(cognitiveEntity));
        }
        
        // Include meta-cognitive information
        if (cognitiveEntity.containsKey("metaCognitiveState")) {
            computationalForm.put("metaData", extractMetaData(cognitiveEntity));
        }
        
        return computationalForm;
    }

    private Object transformToRepresentational(Map<String, Object> cognitiveEntity) {
        Map<String, Object> representationalForm = new HashMap<>();
        
        // Convert cognitive structures to YAML-like representation
        representationalForm.put("cognitive_state", createStateRepresentation(cognitiveEntity));
        representationalForm.put("memory_contents", createMemoryRepresentation(cognitiveEntity));
        representationalForm.put("attention_vectors", createAttentionRepresentation(cognitiveEntity));
        
        return representationalForm;
    }

    @Override
    public boolean supportsTransformation(Domain sourceDomain, Domain targetDomain) {
        return getSupportedDomains().contains(sourceDomain) && 
               getSupportedDomains().contains(targetDomain);
    }

    @Override
    public Map<String, Object> getDomainMetrics(Domain domain) {
        Map<String, Object> domainMetrics = new HashMap<>();
        metrics.forEach((key, value) -> domainMetrics.put(key, value.get()));
        return domainMetrics;
    }

    @Override
    public void validateForDomain(Object entity, Domain domain) {
        if (domain != Domain.COGNITIVE) {
            throw new IllegalArgumentException("Entity not valid for cognitive domain");
        }
        
        if (!(entity instanceof Map)) {
            throw new IllegalArgumentException("Cognitive entity must be a Map");
        }
    }

    @Override
    public Map<String, Object> getDomainOptimizationStrategy(Domain domain) {
        if (domain == Domain.COGNITIVE) {
            return Map.of(
                "attentionAllocation", "adaptive",
                "memoryStrategy", "hierarchical",
                "learningRate", 0.01,
                "metaCognitionEnabled", true
            );
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> toNormalizedForm(Object nativeEntity) {
        return createNormalizedCognitiveForm(nativeEntity);
    }

    @Override
    public Object fromNormalizedForm(Map<String, Object> normalizedForm) {
        return reconstructCognitiveEntity(normalizedForm);
    }

    @Override
    public void validate(Object entity) {
        validateForDomain(entity, Domain.COGNITIVE);
    }

    @Override
    public String getLanguageIdentifier() {
        return "cognitive";
    }

    // Helper methods for cognitive processing
    private Map<String, Object> createWorkingMemoryRepresentation(Object entity) {
        Map<String, Object> workingMemory = new HashMap<>();
        workingMemory.put("focus", calculateFocus(entity));
        workingMemory.put("capacity", calculateCapacity(entity));
        workingMemory.put("chunks", createChunks(entity));
        return workingMemory;
    }

    /**
     * Calculates the attentional focus for a given entity based on multiple factors:
     * - Complexity of the entity structure
     * - Current working memory load
     * - Historical interaction frequency
     * - Relevance to current context
     *
     * @param entity The entity to calculate attention for
     * @return A value between 0.0 and 1.0 representing attentional focus
     * @throws IllegalArgumentException if entity is null
     */
    private double calculateAttentionalFocus(@NotNull Object entity) {
        Objects.requireNonNull(entity, "Entity must not be null");
        
        double complexityScore = calculateComplexityScore(entity);
        double memoryLoadFactor = calculateMemoryLoadFactor();
        double interactionScore = calculateInteractionScore(entity);
        double contextRelevance = calculateContextRelevance(entity);
        
        // Weighted combination of factors
        return (complexityScore * 0.3 +
                memoryLoadFactor * 0.2 +
                interactionScore * 0.2 +
                contextRelevance * 0.3)
                .coerceIn(0.0, 1.0);
    }

    /**
     * Calculates the structural complexity of an entity.
     * Higher complexity increases attention requirements.
     */
    private double calculateComplexityScore(@NotNull Object entity) {
        if (entity instanceof Map) {
            Map<?, ?> mapEntity = (Map<?, ?>) entity;
            int depth = calculateMapDepth(mapEntity);
            int breadth = mapEntity.size();
            return Math.min(0.1 * (depth + breadth), 1.0);
        } else if (entity instanceof Collection) {
            return Math.min(0.05 * ((Collection<?>) entity).size(), 1.0);
        }
        return 0.1; // Base complexity for simple objects
    }

    /**
     * Calculates the current working memory load factor.
     * Higher load reduces available attention capacity.
     */
    private double calculateMemoryLoadFactor() {
        int currentLoad = workingMemory.size();
        int maxCapacity = 7; // Miller's Law - 7±2 items
        return Math.min((double) currentLoad / maxCapacity, 1.0);
    }

    /**
     * Calculates the historical interaction score for an entity.
     * Frequently accessed entities receive more attention.
     */
    private double calculateInteractionScore(@NotNull Object entity) {
        String memoryKey = generateMemoryKey(entity);
        AtomicLong accessCount = metrics.getOrDefault("workingMemoryAccess", new AtomicLong(0));
        return Math.min(accessCount.get() * 0.1, 1.0);
    }

    /**
     * Calculates the relevance of an entity to the current processing context.
     * More relevant entities receive higher attention.
     */
    private double calculateContextRelevance(@NotNull Object entity) {
        // Implementation depends on current context tracking
        Optional<Object> currentContext = getCurrentContext();
        if (currentContext.isPresent()) {
            return calculateSimilarity(entity, currentContext.get());
        }
        return 0.5; // Default medium relevance when context is unavailable
    }

    /**
     * Gets the current processing context if available.
     */
    private Optional<Object> getCurrentContext() {
        return Optional.ofNullable(workingMemory.get("currentContext"));
    }

    /**
     * Calculates similarity between two objects for context relevance.
     */
    private double calculateSimilarity(@NotNull Object entity1, @NotNull Object entity2) {
        // Basic implementation - can be enhanced with more sophisticated similarity metrics
        if (entity1.getClass() == entity2.getClass()) {
            if (entity1 instanceof Map) {
                return calculateMapSimilarity((Map<?, ?>) entity1, (Map<?, ?>) entity2);
            }
            return 0.7; // Same type but not map - moderate similarity
        }
        return 0.3; // Different types - low similarity
    }

    /**
     * Calculates the similarity between two maps based on shared keys and values.
     */
    private double calculateMapSimilarity(@NotNull Map<?, ?> map1, @NotNull Map<?, ?> map2) {
        Set<?> sharedKeys = new HashSet<>(map1.keySet());
        sharedKeys.retainAll(map2.keySet());
        return Math.min((double) sharedKeys.size() / Math.max(map1.size(), map2.size()), 1.0);
    }

    /**
     * Calculates the depth of a nested map structure.
     */
    private int calculateMapDepth(@NotNull Map<?, ?> map) {
        int maxDepth = 1;
        for (Object value : map.values()) {
            if (value instanceof Map) {
                maxDepth = Math.max(maxDepth, 1 + calculateMapDepth((Map<?, ?>) value));
            }
        }
        return maxDepth;
    }

    // Extension method to ensure double values stay within range
    private static double coerceIn(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    private Map<String, Object> createMetaCognitiveState(Object entity) {
        Map<String, Object> metaState = new HashMap<>();
        metaState.put("awareness", calculateAwareness(entity));
        metaState.put("reflection", createReflection(entity));
        metaState.put("adaptation", calculateAdaptation(entity));
        return metaState;
    }

    private boolean meetAttentionThreshold(Map<String, Object> cognitiveForm) {
        double attentionalFocus = (double) cognitiveForm.get("attentionalFocus");
        return attentionalFocus >= attentionThreshold;
    }

    private String generateMemoryKey(Object entity) {
        return entity.hashCode() + "-" + System.nanoTime();
    }

    private List<Map<String, Object>> extractActiveNodes(Map<String, Object> cognitiveEntity) {
        @SuppressWarnings("unchecked")
        Map<String, Object> workingMem = (Map<String, Object>) cognitiveEntity.get("workingMemory");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chunks = (List<Map<String, Object>>) workingMem.get("chunks");
        return new ArrayList<>(chunks);
    }

    private Map<String, Object> extractProcessingState(Map<String, Object> cognitiveEntity) {
        Map<String, Object> state = new HashMap<>();
        state.put("focus", cognitiveEntity.get("attentionalFocus"));
        state.put("processingDepth", calculateProcessingDepth(cognitiveEntity));
        return state;
    }

    private Map<String, Object> extractMetaData(Map<String, Object> cognitiveEntity) {
        @SuppressWarnings("unchecked")
        Map<String, Object> metaState = (Map<String, Object>) cognitiveEntity.get("metaCognitiveState");
        return new HashMap<>(metaState);
    }

    private Map<String, Object> createStateRepresentation(Map<String, Object> cognitiveEntity) {
        Map<String, Object> state = new HashMap<>();
        state.put("attention", cognitiveEntity.get("attentionalFocus"));
        state.put("awareness", extractAwareness(cognitiveEntity));
        return state;
    }

    private Map<String, Object> createMemoryRepresentation(Map<String, Object> cognitiveEntity) {
        @SuppressWarnings("unchecked")
        Map<String, Object> workingMem = (Map<String, Object>) cognitiveEntity.get("workingMemory");
        return new HashMap<>(workingMem);
    }

    private List<Map<String, Object>> createAttentionRepresentation(Map<String, Object> cognitiveEntity) {
        List<Map<String, Object>> vectors = new ArrayList<>();
        // Create attention vectors based on cognitive state
        return vectors;
    }

    private Map<String, Object> createNormalizedCognitiveForm(Object nativeEntity) {
        if (nativeEntity instanceof Map) {
            return new HashMap<>((Map<?, ?>) nativeEntity);
        }
        return Map.of("cognitiveValue", nativeEntity);
    }

    private Object reconstructCognitiveEntity(Map<String, Object> normalizedForm) {
        if (normalizedForm.size() == 1 && normalizedForm.containsKey("cognitiveValue")) {
            return normalizedForm.get("cognitiveValue");
        }
        return new HashMap<>(normalizedForm);
    }

    private double calculateFocus(Object entity) {
        // Implement focus calculation based on entity properties
        return 0.7; // Placeholder implementation
    }

    private int calculateCapacity(Object entity) {
        // Implement capacity calculation
        return 7; // Miller's Law - 7±2 chunks
    }

    private List<Map<String, Object>> createChunks(Object entity) {
        List<Map<String, Object>> chunks = new ArrayList<>();
        // Implement chunking logic
        return chunks;
    }

    private double calculateAwareness(Object entity) {
        // Implement awareness calculation
        return 0.85; // Placeholder implementation
    }

    private Map<String, Object> createReflection(Object entity) {
        // Implement reflection creation
        return new HashMap<>();
    }

    private double calculateAdaptation(Object entity) {
        // Implement adaptation calculation
        return 0.9; // Placeholder implementation
    }

    private double calculateProcessingDepth(Map<String, Object> cognitiveEntity) {
        // Implement processing depth calculation
        return 0.75; // Placeholder implementation
    }

    private Map<String, Object> extractAwareness(Map<String, Object> cognitiveEntity) {
        // Implement awareness extraction
        return new HashMap<>();
    }
} 