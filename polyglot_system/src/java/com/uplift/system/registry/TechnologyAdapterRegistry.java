package com.uplift.system.registry;

import com.uplift.system.gate.*;
import com.uplift.system.adapters.*;
import java.util.*;

/**
 * Registry for managing technology-specific adapters and connectors
 */
public class TechnologyAdapterRegistry {
    private final Map<String, Object> connectors;
    private final Map<String, Object> adapters;
    private static TechnologyAdapterRegistry instance;

    private TechnologyAdapterRegistry() {
        this.connectors = new HashMap<>();
        this.adapters = new HashMap<>();
        initializeDefaultComponents();
    }

    public static synchronized TechnologyAdapterRegistry getInstance() {
        if (instance == null) {
            instance = new TechnologyAdapterRegistry();
        }
        return instance;
    }

    private void initializeDefaultComponents() {
        // Register default connectors
        registerConnector("Java", new JavaConnector());
        registerConnector("C++", new CppConnector());

        // Register default adapters
        registerAdapter("Java", new JavaAdapter());
        registerAdapter("C++", new CppAdapter());
    }

    public void registerConnector(String technology, Object connector) {
        connectors.put(technology, connector);
    }

    public void registerAdapter(String technology, Object adapter) {
        adapters.put(technology, adapter);
    }

    public Object getConnector(String technology) {
        return connectors.get(technology);
    }

    public Object getAdapter(String technology) {
        return adapters.get(technology);
    }

    public Set<String> getSupportedTechnologies() {
        return new HashSet<>(connectors.keySet());
    }

    public boolean isTechnologySupported(String technology) {
        return connectors.containsKey(technology) && adapters.containsKey(technology);
    }
} 