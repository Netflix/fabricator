package com.netflix.fabricator.archaius;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicProperty;
import com.netflix.fabricator.ConfigurationSource;
import com.netflix.fabricator.properties.AbstractPropertiesConfigurationSource;
import com.netflix.fabricator.supplier.ListenableSupplier;

public class ArchaiusConfigurationSource extends AbstractPropertiesConfigurationSource {
    private static final Logger LOG = LoggerFactory.getLogger(ArchaiusConfigurationSource.class);
    
    private final AbstractConfiguration config;
    
    public static ArchaiusConfigurationSource forPrefix(String prefix) {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        String type = config.getString(prefix + "type");
        String id = StringUtils.substringAfterLast(prefix, ".");
        return new ArchaiusConfigurationSource(id, type, config, prefix);
    }
    
    public ArchaiusConfigurationSource(String id, String type, AbstractConfiguration config, String prefix) {
        super(id, type, prefix);
        this.config = config;
    }

    public ArchaiusConfigurationSource(String id, String type, AbstractConfiguration config) {
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
    public <T> ListenableSupplier<T> getDynamicValue(final String propertyName, Class<T> type) {
        final DynamicProperty prop = DynamicProperty.getInstance(getPrefix() + propertyName);
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
                    if (config.containsKey(getPrefix() + propertyName)) {
                        throw new RuntimeException(propertyName + " is not a root for a properties structure");
                    }
                    
                    String prefix = getPrefix() + propertyName;
                    Properties result = new Properties();
                    for (String prop : Lists.newArrayList(config.getKeys(prefix))) {
                        if (prop.startsWith(prefix)) {
                            result.setProperty(prop.substring(prefix.length()+1), config.getProperty(prop).toString());
                        }
                    }
                    return result;
                }
            };
        }
        else {
            LOG.warn(String.format("Unknown type '%s' for property '%s'", type.getCanonicalName(), propertyName));
        }

        return null;
    }

    @Override
    public ConfigurationSource getChild(String name) {
        String prefix = new StringBuilder().append(getPrefix()).append(name).append(".").toString();
        String typePropertyName = prefix + "type";
        
        return new ArchaiusConfigurationSource(
                null, 
                config.getString(typePropertyName),
                config, 
                prefix);
    }

    @Override
    public boolean isSimpleProperty(String propertyName) {
        if (config.containsKey(getPrefix() + propertyName)) {
            return true;
        }
        return false;   // TODO: Look for sub properties
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return true;
    }

    @Override
    public Set<String> getUnknownProperties(Set<String> supportedProperties) {
        // TODO:
        return Collections.emptySet();
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> type) {
        Supplier<T> supplier = this.getDynamicValue(propertyName, type);
        if (supplier != null)
            return supplier.get();
        return null;
    }
}
