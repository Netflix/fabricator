package com.netflix.fabricator.archaius;

import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.netflix.config.ConfigurationManager;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.TypeConfigurationResolver;
import com.netflix.fabricator.jackson.JacksonComponentConfiguration;

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
    
    // TODO: Inject this
    private AbstractConfiguration config = ConfigurationManager.getConfigInstance();
    
    private final Map<String, ComponentConfigurationResolver> overrides;
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Inject
    public ArchaiusTypeConfigurationResolver(Map<String, ComponentConfigurationResolver> overrides) {
        if (overrides == null) {
            this.overrides = Maps.newHashMap();
        }
        else {
            this.overrides = overrides;
        }
    }
    
    @Override
    public ComponentConfigurationResolver getConfigurationFactory(final String componentType) {
        ComponentConfigurationResolver factory = overrides.get(componentType);
        if (factory != null)
            return factory;
        
        return new ComponentConfigurationResolver() {
            @Override
            public ComponentConfiguration getConfiguration(final String key) {
                String prefix    = String.format(DEFAULT_FORMAT_STRING, key, componentType);
                
                if (config.containsKey(prefix)) {
                    String json = Joiner.on(config.getListDelimiter()).join(config.getStringArray(prefix)).trim();
                    if (!json.isEmpty() && json.startsWith("{") && json.endsWith("}")) {
                        try {
                            JsonNode node = mapper.readTree(json);
                            if (node.get(TYPE_FIELD) == null)
                                throw new Exception("Missing 'type' field");
                            return new JacksonComponentConfiguration(key, node.get(TYPE_FIELD).getTextValue(), node);
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    String.format("Unable to parse json from '%s'. (%s)", 
                                            prefix, 
                                            StringUtils.abbreviate(json, 256)), 
                                    e);
                        }
                    }
                }

                String typeField = Joiner.on(".").join(prefix, TYPE_FIELD);
                String typeValue = config.getString(typeField);
                
                if (componentType == null) {
                    throw new RuntimeException(String.format("Type for '%s' not specified '%s'", typeField, componentType));
                }
                
                return new ArchaiusComponentConfiguration(
                        key,
                        typeValue,
                        config,
                        prefix);
            }

            @Override
            public Map<String, ComponentConfiguration> getAllConfigurations() {
                Map<String, ComponentConfiguration> configs = Maps.newHashMap();
                Iterator<String> keys = config.getKeys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String[] parts = StringUtils.split(key, ".");
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
