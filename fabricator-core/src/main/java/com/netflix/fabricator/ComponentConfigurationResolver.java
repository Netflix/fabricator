package com.netflix.fabricator;

/**
 * The ElementConfigurationResolver encapsulates the configuration naming convention
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
    public ComponentConfiguration getConfiguration(String id);
}
