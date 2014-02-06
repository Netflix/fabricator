package com.netflix.fabricator;


/**
 * API for getting a ConfigurationFactory for a specific type.  Each 
 * ConfigurationFactory provides configuration for instances of the 
 * type identified by a unique key.
 * 
 * There should be only one MainConfigurationFactory in an application.
 * 
 * @author elandau
 */
public interface MainConfigurationFactory {
    public ConfigurationFactory getConfigurationFactory(String type);
}
