package com.netflix.fabricator.jackson;

import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.supplier.ListenableSupplier;

public class JacksonComponentConfiguration implements ConfigurationNode {

    private static Logger LOG = LoggerFactory.getLogger(JacksonComponentConfiguration.class);
    
    public static abstract class StaticListenableSupplier<T> implements ListenableSupplier<T> {
        StaticListenableSupplier() {
        }
        
        @Override
        public void onChange(Function<T, Void> func) {
            throw new IllegalStateException("Change notification not supported");
        }
    }
    
    private final String id;
    private final String type;
    private final JsonNode node;
    
    public JacksonComponentConfiguration(String id, String type, JsonNode node) {
        super();
        this.id = id;
        if (type == null && node.has("type")) {
            this.type = node.get("type").asText();
        }
        else {
            this.type = type;
        }
        this.node = node;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ConfigurationNode getChild(String name) {
        JsonNode child = node.get(name);
        if (child == null)
            return null;
        return new JacksonComponentConfiguration(name, null, child);
    }

    @Override
    public boolean isSingle() {
        Preconditions.checkNotNull(node, String.format("Node for '%s' is null", id));
        return !node.isObject() && !node.isArray();
    }

    @Override
    public boolean hasChild(String propertyName) {
        return node.get(propertyName) != null;
    }

    @Override
    public Set<String> getUnknownProperties(Set<String> supportedProperties) {
        return null;
    }

    @Override
    public <T> T getValue(Class<T> type) {
        Supplier<T> supplier = this.getDynamicValue(type);
        if (supplier != null)
            return supplier.get();
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ListenableSupplier<T> getDynamicValue(Class<T> type) {
        if (node != null) {
            if ( String.class.isAssignableFrom(type) ) {
                return (ListenableSupplier<T>) new StaticListenableSupplier<String>() {
                    @Override
                    public String get() {
                        return node.asText();
                    }
    
                };
            }
            else if ( Boolean.class.isAssignableFrom(type) 
                      || Boolean.TYPE.isAssignableFrom(type) 
                      || boolean.class.equals(type)) {
                return (ListenableSupplier<T>) new StaticListenableSupplier<Boolean>() {
                    @Override
                    public Boolean get() {
                        return node.asBoolean();
                    }
                };
            }
            else if ( Integer.class.isAssignableFrom(type) 
                      || Integer.TYPE.isAssignableFrom(type)
                      || int.class.equals(type)) {
                return (ListenableSupplier<T>) new StaticListenableSupplier<Integer>() {
                    @Override
                    public Integer get() {
                        return node.asInt();
                    }
                };
            }
            else if ( Long.class.isAssignableFrom(type) 
                      || Long.TYPE.isAssignableFrom(type)
                      || long.class.equals(type)) {
                return (ListenableSupplier<T>) new StaticListenableSupplier<Long>() {
                    @Override
                    public Long get() {
                        return node.asLong();
                    }
                };
            }
            else if ( Double.class.isAssignableFrom(type) 
                      || Double.TYPE.isAssignableFrom(type) 
                      || double.class.equals(type)) {
                return (ListenableSupplier<T>) new StaticListenableSupplier<Double>() {
                    @Override
                    public Double get() {
                        return node.asDouble();
                    }
                };
            }
            else if ( Properties.class.isAssignableFrom(type)) {
                return (ListenableSupplier<T>) new StaticListenableSupplier<Properties>() {
                    @Override
                    public Properties get() {
                        Properties result = new Properties();
                        for (String prop : Lists.newArrayList(node.fieldNames())) {
                            result.setProperty(prop, node.get(prop).asText());
                        }
                        return result;
                    }
                };
            }
        }
        
        LOG.warn(String.format("Unknown type '%s' for property '%s'", type.getCanonicalName(), getId()));
        return null;
    }

    @Override
    public String toString() {
        return "JacksonComponentConfiguration [id=" + id + ", type=" + type
                + ", node=" + node + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JacksonComponentConfiguration other = (JacksonComponentConfiguration) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
