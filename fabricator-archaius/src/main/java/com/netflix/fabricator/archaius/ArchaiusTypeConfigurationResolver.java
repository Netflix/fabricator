package com.netflix.fabricator.archaius;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.AbstractConfiguration;

import com.google.common.base.Joiner;
import com.netflix.config.ConfigurationManager;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.TypeConfigurationResolver;

/**
 * Main configuration access using Archaius as the configuration source.
 * Configuration for type and key follows a specific naming convention 
 * unless otherwise specified via a MapBinding of type (string) to a
 * specific ConfigurationFactory.
 * 
 * The naming convention is
 *  ${key}.${type}
 * 
 * Where
 *  'key' is the unique id for the instance of the specified type
 *  'type' is the component interface type and not the type of a specified
 *         implementation of the compoennt
 *  
 * @author elandau
 *
 */
@Singleton
public class ArchaiusTypeConfigurationResolver implements TypeConfigurationResolver {
    private static String DEFAULT_FORMAT_STRING = "%s.%s";
    private static String TYPE_FIELD = "type";
    
    private AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    
    private final Map<String, ComponentConfigurationResolver> overrides;
    
    @Inject
    public ArchaiusTypeConfigurationResolver(Map<String, ComponentConfigurationResolver> overrides) {
        this.overrides = overrides;
    }
    
    @Override
    public ComponentConfigurationResolver getConfigurationFactory(final String type) {
        ComponentConfigurationResolver factory = overrides.get(type);
        if (factory != null)
            return factory;
        
        return new ComponentConfigurationResolver() {
            @Override
            public ComponentConfiguration getConfiguration(final String key) {
                String prefix    = String.format(DEFAULT_FORMAT_STRING, key, type);
                String typeField = Joiner.on(".").join(prefix, TYPE_FIELD);
                String typeValue = config.getString(typeField);
                
                if (type == null) {
                    throw new RuntimeException(String.format("Type for '%s' not specified '%s'", typeField, type));
                }
                
                return new ArchaiusComponentConfiguration(
                        key,
                        typeValue,
                        config,
                        prefix);
            }
        };
    }
}
