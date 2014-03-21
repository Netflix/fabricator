package com.netflix.fabricator.properties;

import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.TypeConfigurationResolver;
import com.netflix.fabricator.jackson.JacksonComponentConfiguration;

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
    
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public PropertiesTypeConfigurationResolver(Properties properties, Map<String, ComponentConfigurationResolver> overrides) {
        if (overrides == null) {
            overrides = Maps.newHashMap();
        }
        this.overrides  = overrides;
        this.properties = properties;
    }
    
    @Override
    public ComponentConfigurationResolver getConfigurationFactory(final String componentType) {
        // Look for overrides
        ComponentConfigurationResolver factory = overrides.get(componentType);
        if (factory != null)
            return factory;
        
        return new ComponentConfigurationResolver() {
            @Override
            public ComponentConfiguration getConfiguration(final String key) {
                String prefix    = String.format(DEFAULT_FORMAT_STRING, key, componentType);
                
                if (properties.containsKey(prefix)) {
                    String json = properties.getProperty(prefix).trim();
                    
                    if (!json.isEmpty() && json.startsWith("{") && json.endsWith("}")) {
                        try {
                            return new JacksonComponentConfiguration(key, componentType, mapper.readTree(json));
                        } catch (Exception e) {
                            throw new RuntimeException(String.format("Unable to parse json from '%s'", prefix));
                        }
                    }
                }
                
                String typeField = Joiner.on(".").join(prefix, TYPE_FIELD);
                String typeValue = properties.getProperty(typeField);
                
                if (componentType == null) {
                    throw new RuntimeException(String.format("Type for '%s' not specified '%s'", typeField, componentType));
                }
                
                return new PropertiesComponentConfiguration(
                        key,
                        typeValue,
                        properties,
                        prefix);
            }
            
            @Override
            public Map<String, ComponentConfiguration> getAllConfigurations() {
                Map<String, ComponentConfiguration> configs = Maps.newHashMap();
                for (Object key : properties.keySet()) {
                    String[] parts = StringUtils.split(key.toString(), ".");
                    if (parts.length > 1) {
                        String type = parts[1];
                        if (type.equals(componentType)) {
                            String id = parts[0];
                            if (!configs.containsKey(id)) {
                                configs.put(id, getConfiguration(id));
                            }
                        }
                    }
                }
                return configs;
            }
        };
    }
}
