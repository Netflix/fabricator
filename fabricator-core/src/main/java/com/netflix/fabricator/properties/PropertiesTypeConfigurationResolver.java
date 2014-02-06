package com.netflix.fabricator.properties;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.TypeConfigurationResolver;

/**
 * TypeConfiguration resolver following the convention of ${id}.${type}. to determine
 * the root configuration context for a component
 * 
 * @author elandau
 *
 */
public class PropertiesTypeConfigurationResolver implements TypeConfigurationResolver {
    private static String DEFAULT_FORMAT_STRING = "%s.%s";
    
    // TOOD: Feature to provide a different field from which to determine the type
    // TODO: Feature to provide entirely custom logic to determine the type
    private static String TYPE_FIELD            = "type";
    
    /**
     * Map of type to ComponentConfigurationResolver overrides in the event that the
     * naming convention used in this class is not adequate
     */
    private final Map<String, ComponentConfigurationResolver> overrides;
    
    /**
     * Properties object containing ALL configuration properties for all components
     * of varying types
     */
    private final Properties properties;
    
    @Inject
    public PropertiesTypeConfigurationResolver(Properties properties, Map<String, ComponentConfigurationResolver> overrides) {
        this.overrides  = overrides;
        this.properties = properties;
    }
    
    @Override
    public ComponentConfigurationResolver getConfigurationFactory(final String type) {
        // Look for overrides
        ComponentConfigurationResolver factory = overrides.get(type);
        if (factory != null)
            return factory;
        
        return new ComponentConfigurationResolver() {
            @Override
            public ComponentConfiguration getConfiguration(final String key) {
                String prefix    = String.format(DEFAULT_FORMAT_STRING, key, type);
                String typeField = Joiner.on(".").join(prefix, TYPE_FIELD);
                String typeValue = properties.getProperty(typeField);
                
                if (type == null) {
                    throw new RuntimeException(String.format("Type for '%s' not specified '%s'", typeField, type));
                }
                
                return new PropertiesComponentConfiguration(
                        key,
                        typeValue,
                        properties,
                        prefix);
            }
        };
    }
}
