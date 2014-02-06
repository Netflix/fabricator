package com.netflix.fabricator.jackson;

import java.util.Properties;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.netflix.fabricator.ConfigurationSource;
import com.netflix.fabricator.jackson.JacksonConfigurationSource;
import com.netflix.fabricator.supplier.ListenableSupplier;

public class JacksonConfigurationSource implements ConfigurationSource {
    private static Logger LOG = LoggerFactory.getLogger(JacksonConfigurationSource.class);
    
    public static abstract class StaticListenableSupplier<T> implements ListenableSupplier<T> {
        StaticListenableSupplier() {
        }
        
        @Override
        public void onChange(Function<T, Void> func) {
            throw new IllegalStateException("Change notification not supported");
        }
    }
    
    private final String key;
    private final String type;
    private final JsonNode node;
    
    public JacksonConfigurationSource(String key, String type, JsonNode node) {
        super();
        this.key = key;
        this.type = type;
        this.node = node;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ConfigurationSource getChild(String name) {
        return new JacksonConfigurationSource(null, null, node.get(name));
    }

    @Override
    public boolean isSimpleProperty(String propertyName) {
        JsonNode child = node.get(propertyName);
        if (child == null)
            return true;
        return !child.isObject();
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return node.get(propertyName) != null;
    }

    @Override
    public Set<String> getUnknownProperties(Set<String> supportedProperties) {
        return null;
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> type) {
        Supplier<T> supplier = this.getDynamicValue(propertyName, type);
        if (supplier != null)
            return supplier.get();
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ListenableSupplier<T> getDynamicValue(final String propertyName, Class<T> type) {
        if ( String.class.isAssignableFrom(type) ) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<String>() {
                @Override
                public String get() {
                    JsonNode child = node.get(propertyName);
                    if (child == null)
                        return null;
                    return child.asText();
                }

            };
        }
        else if ( Boolean.class.isAssignableFrom(type) 
                  || Boolean.TYPE.isAssignableFrom(type) 
                  || boolean.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Boolean>() {
                @Override
                public Boolean get() {
                    JsonNode child = node.get(propertyName);
                    if (child == null)
                        return null;
                    return child.asBoolean();
                }
            };
        }
        else if ( Integer.class.isAssignableFrom(type) 
                  || Integer.TYPE.isAssignableFrom(type)
                  || int.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Integer>() {
                @Override
                public Integer get() {
                    JsonNode child = node.get(propertyName);
                    if (child == null)
                        return null;
                    return child.asInt();
                }
            };
        }
        else if ( Long.class.isAssignableFrom(type) 
                  || Long.TYPE.isAssignableFrom(type)
                  || long.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Long>() {
                @Override
                public Long get() {
                    JsonNode child = node.get(propertyName);
                    if (child == null)
                        return null;
                    return child.asLong();
                }
            };
        }
        else if ( Double.class.isAssignableFrom(type) 
                  || Double.TYPE.isAssignableFrom(type) 
                  || double.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Double>() {
                @Override
                public Double get() {
                    JsonNode child = node.get(propertyName);
                    if (child == null)
                        return null;
                    return child.asDouble();
                }
            };
        }
        else if ( Properties.class.isAssignableFrom(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Properties>() {
                @Override
                public Properties get() {
                    JsonNode child = node.get(propertyName);
                    Properties result = new Properties();
                    for (String prop : Lists.newArrayList(child.getFieldNames())) {
                        result.setProperty(prop, child.get(prop).asText());
                    }
                    return result;
                }
            };
        }
        else {
            LOG.warn(String.format("Unknown type '%s' for property '%s'", type.getCanonicalName(), propertyName));
            return null;
        }
    }

}
