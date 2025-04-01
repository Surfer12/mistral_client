package com.uplift.system.adapters;

import com.uplift.system.events.DomainAwareEventBus.Domain;
import com.uplift.system.config.SystemConfig;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

/**
 * Adapter for the Representational domain focusing on YAML-like structures and symbolic references.
 * This adapter handles the transformation and management of hierarchical data structures with
 * support for anchors, aliases, and reference resolution.
 */
public class RepresentationalDomainAdapter implements DomainAwareAdapter {
    private final SystemConfig config;
    private final Map<String, AtomicLong> metrics;
    private final Map<String, Object> anchorRegistry;
    private final Map<String, List<String>> referenceGraph;

    /**
     * Constructs a new RepresentationalDomainAdapter with the specified configuration.
     *
     * @param config The system configuration containing representational domain settings
     * @throws IllegalArgumentException if config is null
     */
    public RepresentationalDomainAdapter(@NotNull SystemConfig config) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.metrics = new ConcurrentHashMap<>();
        this.anchorRegistry = new ConcurrentHashMap<>();
        this.referenceGraph = new ConcurrentHashMap<>();
        initializeMetrics();
    }

    private void initializeMetrics() {
        metrics.put("anchorDefinitions", new AtomicLong(0));
        metrics.put("referenceResolutions", new AtomicLong(0));
        metrics.put("structureTransformations", new AtomicLong(0));
        metrics.put("compressionRatio", new AtomicLong(0));
    }

    @Override
    @NotNull
    public Set<Domain> getSupportedDomains() {
        return EnumSet.of(Domain.REPRESENTATIONAL, Domain.COMPUTATIONAL, Domain.COGNITIVE);
    }

    @Override
    @NotNull
    public Object transformBetweenDomains(@NotNull Object entity, @NotNull Domain sourceDomain, @NotNull Domain targetDomain) {
        Objects.requireNonNull(entity, "Entity must not be null");
        Objects.requireNonNull(sourceDomain, "Source domain must not be null");
        Objects.requireNonNull(targetDomain, "Target domain must not be null");
        
        metrics.get("structureTransformations").incrementAndGet();
        
        if (sourceDomain == Domain.REPRESENTATIONAL) {
            return transformFromRepresentational(entity, targetDomain);
        } else if (targetDomain == Domain.REPRESENTATIONAL) {
            return transformToRepresentational(entity, sourceDomain);
        }
        
        throw new UnsupportedOperationException("Unsupported domain transformation");
    }

    /**
     * Creates a YAML-like structure with anchors and references from a source entity.
     *
     * @param entity The source entity to transform
     * @param sourceDomain The domain of the source entity
     * @return A Map containing the YAML structure with anchors and references
     */
    private Object transformToRepresentational(@NotNull Object entity, @NotNull Domain sourceDomain) {
        Map<String, Object> yamlStructure = new HashMap<>();
        
        // Create YAML-like structure with anchors and references
        yamlStructure.put("structure", createStructure(entity));
        yamlStructure.put("anchors", defineAnchors(entity));
        yamlStructure.put("references", createReferences(entity));
        
        // Register anchors for future reference resolution
        registerAnchors(yamlStructure);
        
        // Calculate and update compression metrics
        updateCompressionMetrics(entity, yamlStructure);
        
        return yamlStructure;
    }

    /**
     * Updates compression metrics based on original and transformed structures.
     */
    private void updateCompressionMetrics(@NotNull Object original, @NotNull Map<String, Object> transformed) {
        int originalSize = calculateStructureSize(original);
        int transformedSize = calculateStructureSize(transformed.get("structure"));
        
        if (originalSize > 0) {
            double ratio = (double) transformedSize / originalSize;
            metrics.get("compressionRatio").set((long) (ratio * 100));
        }
    }

    /**
     * Calculates the size of a structure recursively.
     */
    private int calculateStructureSize(@NotNull Object structure) {
        if (structure instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) structure;
            return map.entrySet().stream()
                .mapToInt(entry -> calculateStructureSize(entry.getKey()) + calculateStructureSize(entry.getValue()))
                .sum();
        } else if (structure instanceof Collection) {
            Collection<?> collection = (Collection<?>) structure;
            return collection.stream()
                .mapToInt(this::calculateStructureSize)
                .sum();
        }
        return 1;
    }

    /**
     * Creates the main structure of the YAML representation, identifying potential anchors.
     *
     * @param entity The source entity to structure
     * @return A Map containing the structured representation
     */
    private Map<String, Object> createStructure(@NotNull Object entity) {
        Map<String, Object> structure = new HashMap<>();
        
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = (Map<String, Object>) entity;
            
            // Create hierarchical structure with anchor detection
            sourceMap.forEach((key, value) -> {
                if (isAnchorCandidate(value)) {
                    String anchor = createAnchor(key, value);
                    structure.put(key, Map.of("&" + anchor, value));
                    metrics.get("anchorDefinitions").incrementAndGet();
                } else {
                    structure.put(key, value);
                }
            });
        }
        
        return structure;
    }

    /**
     * Determines if a value should be considered as an anchor candidate.
     * Values are considered anchor candidates if they are complex objects
     * that might be referenced multiple times.
     *
     * @param value The value to check
     * @return true if the value should be an anchor
     */
    private boolean isAnchorCandidate(@NotNull Object value) {
        if (value instanceof Map) {
            return ((Map<?, ?>) value).size() > 2;
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).size() > 2;
        }
        return false;
    }

    /**
     * Creates a unique anchor identifier for a given key-value pair.
     *
     * @param key The key associated with the value
     * @param value The value to create an anchor for
     * @return A unique anchor identifier
     */
    private String createAnchor(@NotNull String key, @NotNull Object value) {
        String baseAnchor = key.replaceAll("[^a-zA-Z0-9]", "_");
        String valueHash = String.valueOf(Objects.hash(value));
        return baseAnchor + "_" + valueHash;
    }

    private Object transformFromRepresentational(Object entity, Domain targetDomain) {
        if (!(entity instanceof Map)) {
            throw new IllegalArgumentException("Representational entity must be a Map");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> yamlEntity = (Map<String, Object>) entity;
        
        if (targetDomain == Domain.COMPUTATIONAL) {
            return transformToComputational(yamlEntity);
        } else if (targetDomain == Domain.COGNITIVE) {
            return transformToCognitive(yamlEntity);
        }
        
        throw new UnsupportedOperationException("Unsupported target domain: " + targetDomain);
    }

    private Object transformToComputational(Map<String, Object> yamlEntity) {
        Map<String, Object> computationalForm = new HashMap<>();
        
        // Convert YAML structures to computational representations
        computationalForm.put("dataStructure", extractDataStructure(yamlEntity));
        computationalForm.put("pointers", convertReferencesToPointers(yamlEntity));
        computationalForm.put("metadata", extractMetadata(yamlEntity));
        
        return computationalForm;
    }

    private Object transformToCognitive(Map<String, Object> yamlEntity) {
        Map<String, Object> cognitiveForm = new HashMap<>();
        
        // Convert YAML structures to cognitive representations
        cognitiveForm.put("chunks", createCognitiveChunks(yamlEntity));
        cognitiveForm.put("associations", extractAssociations(yamlEntity));
        cognitiveForm.put("hierarchy", buildHierarchy(yamlEntity));
        
        return cognitiveForm;
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
        
        // Add YAML-specific metrics
        domainMetrics.put("activeAnchors", anchorRegistry.size());
        domainMetrics.put("referenceGraphSize", calculateReferenceGraphSize());
        
        return domainMetrics;
    }

    @Override
    public void validateForDomain(Object entity, Domain domain) {
        if (domain != Domain.REPRESENTATIONAL) {
            throw new IllegalArgumentException("Entity not valid for representational domain");
        }
        
        if (!(entity instanceof Map)) {
            throw new IllegalArgumentException("Representational entity must be a Map");
        }
        
        validateYamlStructure((Map<?, ?>) entity);
    }

    @Override
    public Map<String, Object> getDomainOptimizationStrategy(Domain domain) {
        if (domain == Domain.REPRESENTATIONAL) {
            return Map.of(
                "compressionStrategy", "reference-based",
                "anchorPlacement", "optimal",
                "referenceResolution", "lazy",
                "structureSharing", true
            );
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> toNormalizedForm(Object nativeEntity) {
        return createNormalizedYamlForm(nativeEntity);
    }

    @Override
    public Object fromNormalizedForm(Map<String, Object> normalizedForm) {
        return reconstructYamlEntity(normalizedForm);
    }

    @Override
    public void validate(Object entity) {
        validateForDomain(entity, Domain.REPRESENTATIONAL);
    }

    @Override
    public String getLanguageIdentifier() {
        return "yaml";
    }

    // Helper methods for YAML processing
    private Map<String, Object> defineAnchors(Object entity) {
        Map<String, Object> anchors = new HashMap<>();
        
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = (Map<String, Object>) entity;
            
            // Define anchors for reusable elements
            sourceMap.forEach((key, value) -> {
                if (isAnchorCandidate(value)) {
                    String anchor = createAnchor(key, value);
                    anchors.put(anchor, value);
                    metrics.get("anchorDefinitions").incrementAndGet();
                }
            });
        }
        
        return anchors;
    }

    private Map<String, Object> createReferences(Object entity) {
        Map<String, Object> references = new HashMap<>();
        
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = (Map<String, Object>) entity;
            
            // Create references to anchored elements
            sourceMap.forEach((key, value) -> {
                if (hasReference(value)) {
                    String reference = resolveReference(value);
                    references.put(key, "*" + reference);
                    metrics.get("referenceResolutions").incrementAndGet();
                }
            });
        }
        
        return references;
    }

    private void registerAnchors(Map<String, Object> yamlStructure) {
        @SuppressWarnings("unchecked")
        Map<String, Object> anchors = (Map<String, Object>) yamlStructure.get("anchors");
        
        if (anchors != null) {
            anchors.forEach((anchor, value) -> {
                anchorRegistry.put(anchor, value);
                updateReferenceGraph(anchor);
            });
        }
    }

    private Map<String, Object> extractDataStructure(Map<String, Object> yamlEntity) {
        Map<String, Object> dataStructure = new HashMap<>();
        
        if (yamlEntity.containsKey("structure")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> structure = (Map<String, Object>) yamlEntity.get("structure");
            
            // Convert YAML structure to computational form
            structure.forEach((key, value) -> {
                if (value instanceof Map && ((Map<?, ?>) value).containsKey("&")) {
                    // Handle anchored values
                    String anchor = (String) ((Map<?, ?>) value).keySet().iterator().next();
                    dataStructure.put(key, resolveAnchorValue(anchor.substring(1)));
                } else {
                    dataStructure.put(key, value);
                }
            });
        }
        
        return dataStructure;
    }

    private Map<String, Object> convertReferencesToPointers(Map<String, Object> yamlEntity) {
        Map<String, Object> pointers = new HashMap<>();
        
        if (yamlEntity.containsKey("references")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> references = (Map<String, Object>) yamlEntity.get("references");
            
            // Convert YAML references to pointer-like structures
            references.forEach((key, value) -> {
                if (value instanceof String && ((String) value).startsWith("*")) {
                    String reference = ((String) value).substring(1);
                    pointers.put(key, createPointer(reference));
                }
            });
        }
        
        return pointers;
    }

    private List<Map<String, Object>> createCognitiveChunks(Map<String, Object> yamlEntity) {
        List<Map<String, Object>> chunks = new ArrayList<>();
        
        if (yamlEntity.containsKey("structure")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> structure = (Map<String, Object>) yamlEntity.get("structure");
            
            // Create cognitive chunks from YAML structure
            structure.forEach((key, value) -> {
                Map<String, Object> chunk = new HashMap<>();
                chunk.put("concept", key);
                chunk.put("content", value);
                chunk.put("associations", findAssociations(key, value));
                chunks.add(chunk);
            });
        }
        
        return chunks;
    }

    private List<String> extractAssociations(Map<String, Object> yamlEntity) {
        List<String> associations = new ArrayList<>();
        
        if (yamlEntity.containsKey("references")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> references = (Map<String, Object>) yamlEntity.get("references");
            
            // Extract associations from references
            references.forEach((key, value) -> {
                if (value instanceof String && ((String) value).startsWith("*")) {
                    String reference = ((String) value).substring(1);
                    associations.add(key + " -> " + reference);
                }
            });
        }
        
        return associations;
    }

    private Map<String, Object> buildHierarchy(Map<String, Object> yamlEntity) {
        Map<String, Object> hierarchy = new HashMap<>();
        
        if (yamlEntity.containsKey("structure")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> structure = (Map<String, Object>) yamlEntity.get("structure");
            
            // Build hierarchical representation
            hierarchy.put("root", createHierarchyNode(structure));
        }
        
        return hierarchy;
    }

    private Map<String, Object> createNormalizedYamlForm(Object nativeEntity) {
        if (nativeEntity instanceof Map) {
            return new HashMap<>((Map<?, ?>) nativeEntity);
        }
        return Map.of("value", nativeEntity);
    }

    private Object reconstructYamlEntity(Map<String, Object> normalizedForm) {
        if (normalizedForm.size() == 1 && normalizedForm.containsKey("value")) {
            return normalizedForm.get("value");
        }
        return new HashMap<>(normalizedForm);
    }

    // Private utility methods
    private boolean hasReference(Object value) {
        return value instanceof Map && ((Map<?, ?>) value).containsKey("&");
    }

    private String resolveReference(Object value) {
        return ((String) ((Map<?, ?>) value).keySet().iterator().next()).substring(1);
    }

    private void updateReferenceGraph(String anchor) {
        referenceGraph.computeIfAbsent(anchor, k -> new ArrayList<>());
    }

    private Object resolveAnchorValue(String anchor) {
        return anchorRegistry.get(anchor);
    }

    private Map<String, Object> createPointer(String reference) {
        return Map.of("pointer", reference, "type", "reference");
    }

    private List<String> findAssociations(String key, Object value) {
        List<String> associations = new ArrayList<>();
        if (value instanceof Map) {
            ((Map<?, ?>) value).keySet().forEach(k -> 
                associations.add(key + "." + k));
        }
        return associations;
    }

    private Map<String, Object> createHierarchyNode(Map<String, Object> structure) {
        Map<String, Object> node = new HashMap<>();
        node.put("content", structure);
        node.put("children", new ArrayList<>());
        return node;
    }

    private void validateYamlStructure(Map<?, ?> entity) {
        // Validate basic YAML structure requirements
        if (!entity.containsKey("structure")) {
            throw new IllegalArgumentException("Missing required 'structure' key");
        }
    }

    private int calculateReferenceGraphSize() {
        return referenceGraph.values().stream()
            .mapToInt(List::size)
            .sum();
    }
} 