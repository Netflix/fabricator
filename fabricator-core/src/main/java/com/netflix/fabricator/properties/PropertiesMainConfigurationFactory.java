package com.netflix.fabricator.properties;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.netflix.fabricator.ConfigurationFactory;
import com.netflix.fabricator.ConfigurationSource;
import com.netflix.fabricator.MainConfigurationFactory;

public class PropertiesMainConfigurationFactory implements MainConfigurationFactory {
    private static String DEFAULT_FORMAT_STRING = "%s.%s";
    private static String TYPE_FIELD            = "type";
    
    private final Map<String, ConfigurationFactory> factories;
    private final Properties properties;
    
    @Inject
    public PropertiesMainConfigurationFactory(Properties properties, Map<String, ConfigurationFactory> factories) {
        this.factories  = factories;
        this.properties = properties;
    }
    
    @Override
    public ConfigurationFactory getConfigurationFactory(final String type) {
        ConfigurationFactory factory = factories.get(type);
        if (factory != null)
            return factory;
        
        return new ConfigurationFactory() {
            @Override
            public ConfigurationSource getConfiguration(final String key) {
                String prefix    = String.format(DEFAULT_FORMAT_STRING, key, type);
                String typeField = Joiner.on(".").join(prefix, TYPE_FIELD);
                String typeValue = properties.getProperty(typeField);
                
                if (type == null) {
                    throw new RuntimeException(String.format("Type for '%s' not specified '%s'", typeField, type));
                }
                
                return new PropertiesConfigurationSource(
                        key,
                        typeValue,
                        properties,
                        prefix);
            }
        };
    }
}
