package com.netflix.fabricator;

/**
 * The configuration factory creates a ConfigurationSource from a configuration 
 * that is rooted at the provided key.  @see PropertyMapperFactory for an example
 * 
 * @author elandau
 */
public interface ConfigurationFactory {
    /**
     * Return a configuration for the specified type with root context for the specified key
     * 
     * @param key
     * @return
     */
    public ConfigurationSource getConfiguration(String key);
}
