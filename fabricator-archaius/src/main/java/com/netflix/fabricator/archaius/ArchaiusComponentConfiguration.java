package com.netflix.fabricator.archaius;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.properties.AbstractPropertiesComponentConfiguration;
import com.netflix.fabricator.supplier.ListenableSupplier;

public class ArchaiusComponentConfiguration extends AbstractPropertiesComponentConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(ArchaiusComponentConfiguration.class);
    
    public static ArchaiusComponentConfiguration forPrefix(String prefix) {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        String type = config.getString(prefix + "type");
        String id = StringUtils.substringAfterLast(prefix, ".");
        return new ArchaiusComponentConfiguration(id, type, config, prefix);
    }
    
    private final AbstractConfiguration config;
    
    public ArchaiusComponentConfiguration(String id, String type, AbstractConfiguration config, String prefix) {
        super(id, type, prefix);
        this.config = config;
    }

    public ArchaiusComponentConfiguration(String id, String type, AbstractConfiguration config) {
        super(id, type);
        this.config = config;
    }

    public static abstract class DynamicListenableSupplier<T> implements ListenableSupplier<T> {
        private final DynamicProperty prop;
        
        DynamicListenableSupplier(DynamicProperty prop) {
            this.prop = prop;
        }
        
        @Override
        public void onChange(final Function<T, Void> func) {
            prop.addCallback(new Runnable() {
                @Override
                public void run() {
                    func.apply(get());
                }
            });
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> ListenableSupplier<T> getDynamicValue(Class<T> type) {
        final DynamicProperty prop = DynamicProperty.getInstance(getFullName());
        if ( String.class.isAssignableFrom(type) ) {
            return (ListenableSupplier<T>) new DynamicListenableSupplier<String>(prop) {
                @Override
                public String get() {
                    return prop.getString();
                }
            };
        }
        else if ( Boolean.class.isAssignableFrom(type) || 
                  Boolean.TYPE.isAssignableFrom(type) || 
                  boolean.class.equals(type)) {
            return (ListenableSupplier<T>) new DynamicListenableSupplier<Boolean>(prop) {
                @Override
                public Boolean get() {
                    return prop.getBoolean();
                }
            };
        }
        else if ( Integer.class.isAssignableFrom(type) 
                  || Integer.TYPE.isAssignableFrom(type) 
                  || int.class.equals(type))
        {
            return (ListenableSupplier<T>) new DynamicListenableSupplier<Integer>(prop) {
                @Override
                public Integer get() {
                    return prop.getInteger();
                }
            };
        }
        else if ( Long.class.isAssignableFrom(type) 
                  || Long.TYPE.isAssignableFrom(type) 
                  || long.class.equals(type))
        {
            return (ListenableSupplier<T>) new DynamicListenableSupplier<Long>(prop) {
                @Override
                public Long get() {
                    return prop.getLong();
                }
            };
        }
        else if ( Double.class.isAssignableFrom(type) 
                  || Double.TYPE.isAssignableFrom(type) 
                  || double.class.equals(type))
        {
            return (ListenableSupplier<T>) new DynamicListenableSupplier<Double>(prop) {
                @Override
                public Double get() {
                    return prop.getDouble();
                }
            };
        }
        else if ( Properties.class.isAssignableFrom(type)) {
            return (ListenableSupplier<T>) new DynamicListenableSupplier<Properties>(prop) {
                @Override
                public Properties get() {
                    if (config.containsKey(getFullName())) {
                        throw new RuntimeException(getFullName() + " is not a root for a properties structure");
                    }
                    
                    String prefix = getFullName() + ".";
                    Properties result = new Properties();
                    for (String prop : Lists.newArrayList(config.getKeys(prefix))) {
                        result.setProperty(prop, config.getString(prop));
                    }
                    return result;
                }
            };
        }
        else {
            LOG.warn(String.format("Unknown type '%s' for property '%s'", type.getCanonicalName(), getFullName()));
        }

        return null;
    }

    @Override
    public ConfigurationNode getChild(String name) {
        String fullName = Joiner.on(".").skipNulls().join(getFullName(), name);
        
        return new ArchaiusComponentConfiguration(
                name, 
                config.getString(Joiner.on(".").skipNulls().join(fullName, "type")),
                config, 
                fullName);
    }

    @Override
    public boolean isSingle() {
        if (config.containsKey(getFullName())) {
            return true;
        }
        return false;   // TODO: Look for sub properties
    }

    @Override
    public boolean hasChild(String propertyName) {
        return true;
    }

    @Override
    public Set<String> getUnknownProperties(Set<String> supportedProperties) {
        // TODO:
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue(Class<T> type) {
        String key = getFullName();
        if ( String.class.isAssignableFrom(type) ) {
            return (T) config.getString(key);
        }
        else if ( Boolean.class.isAssignableFrom(type) || 
                  Boolean.TYPE.isAssignableFrom(type) || 
                  boolean.class.equals(type)) {
            return (T) config.getBoolean(key, null);
        }
        else if ( Integer.class.isAssignableFrom(type) 
                  || Integer.TYPE.isAssignableFrom(type) 
                  || int.class.equals(type)) {
            return (T) config.getInteger(key, null);
        }
        else if ( Long.class.isAssignableFrom(type) 
                  || Long.TYPE.isAssignableFrom(type) 
                  || long.class.equals(type)) {
            return (T) config.getLong(key, null);
        }
        else if ( Double.class.isAssignableFrom(type) 
                  || Double.TYPE.isAssignableFrom(type) 
                  || double.class.equals(type))  {
            return (T) config.getDouble(key, null);
        }
        else if ( Short.class.isAssignableFrom(type) 
                || Short.TYPE.isAssignableFrom(type) 
                || short.class.equals(type)) {
            return (T) config.getShort(key, null);
        }
        else if ( Float.class.isAssignableFrom(type) 
                || Float.TYPE.isAssignableFrom(type) 
                || float.class.equals(type)) {
            return (T) config.getFloat(key, null);
        }
        else if ( BigDecimal.class.isAssignableFrom(type) ) {
            return (T) config.getBigDecimal(key, null);
        }
        else if ( BigInteger.class.isAssignableFrom(type) ) {
            return (T) config.getBigInteger(key, null);
        }
        else if ( Properties.class.isAssignableFrom(type)) {
            String prefix = getFullName();
            Properties result = new Properties();
            Configuration sub = config.subset(prefix);
            Iterator<String> iter = sub.getKeys();
            while (iter.hasNext()) {
                String propName = iter.next();
                result.setProperty(propName, sub.getString(propName));
            }
            return (T) result;
        }
        else {
            LOG.warn(String.format("Unknown type '%s' for property '%s'", type.getCanonicalName(), getFullName()));
            return null;
        }
    }
}
