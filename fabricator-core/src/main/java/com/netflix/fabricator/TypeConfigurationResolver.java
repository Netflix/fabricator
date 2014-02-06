package com.netflix.fabricator;


/**
 * API for resolving the ComponentConfigurationResolver for a specified type
 * A concrete TypeConfigurationResolver will normally encapsulate an entire
 * configuration file that holds configurations for multiple components of
 * varying types.
 * 
 * @see PropertiesTypeConfigurationResolver
 * 
 * @author elandau
 */
public interface TypeConfigurationResolver {
    public ComponentConfigurationResolver getConfigurationFactory(String type);
}
