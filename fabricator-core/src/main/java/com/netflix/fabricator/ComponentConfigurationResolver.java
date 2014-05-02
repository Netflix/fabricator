package com.netflix.fabricator;

import java.util.Map;

/**
 * The ComponentConfigurationResolver encapsulates the configuration naming convention
 * for a specific type of components.  The resolver returns the ComponentConfiguration
 * for an id of it's type using whatever naming convension it chooses.
 * 
 * @author elandau
 */
public interface ComponentConfigurationResolver {
    /**
     * Return a configuration for the specified type with root context for the specified key
     * 
     * @param id
     * @return
     */
    public ConfigurationNode getConfiguration(String id);
    
    /**
     * Return a map of ALL component configurations for this type.
     * 
     * @return A map where key=id and value=the configuration
     */
    public Map<String, ConfigurationNode> getAllConfigurations();
}
