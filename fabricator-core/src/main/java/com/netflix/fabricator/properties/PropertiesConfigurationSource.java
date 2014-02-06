package com.netflix.fabricator.properties;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.netflix.fabricator.ConfigurationSource;
import com.netflix.fabricator.supplier.ListenableSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class PropertiesConfigurationSource extends AbstractPropertiesConfigurationSource {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesConfigurationSource.class);

    private final Properties props;
    
    public PropertiesConfigurationSource(String id, String type, Properties props, String prefix) {
        super(id, type, prefix);
        this.props = props;
    }

    public PropertiesConfigurationSource(String id, String type, Properties props) {
        super(id, type);
        this.props = props;
    }

    public static abstract class StaticListenableSupplier<T> implements ListenableSupplier<T> {
        StaticListenableSupplier() {
        }
        
        @Override
        public void onChange(Function<T, Void> func) {
            // noop
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> ListenableSupplier<T> getDynamicValue(final String key, Class<T> type) {
        final String propertyName = getPrefix() + key;
        if ( String.class.isAssignableFrom(type) ) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<String>() {
                @Override
                public String get() {
                    final String value = props.getProperty(propertyName);
                    if (value == null)
                        return null;
                    return value;
                }

            };
        }
        else if ( Boolean.class.isAssignableFrom(type) 
                  || Boolean.TYPE.isAssignableFrom(type) 
                  || boolean.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Boolean>() {
                @Override
                public Boolean get() {
                    final String value = props.getProperty(propertyName);
                    if (value == null)
                        return null;
                    return Boolean.valueOf(value);
                }
            };
        }
        else if ( Integer.class.isAssignableFrom(type) 
                  || Integer.TYPE.isAssignableFrom(type)
                  || int.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Integer>() {
                @Override
                public Integer get() {
                    final String value = props.getProperty(propertyName);
                    if (value == null)
                        return null;
                    return Integer.valueOf(value);
                }
            };
        }
        else if ( Long.class.isAssignableFrom(type) 
                  || Long.TYPE.isAssignableFrom(type)
                  || long.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Long>() {
                @Override
                public Long get() {
                    final String value = props.getProperty(propertyName);
                    if (value == null)
                        return null;
                    return Long.valueOf(value);
                }
            };
        }
        else if ( Double.class.isAssignableFrom(type) 
                  || Double.TYPE.isAssignableFrom(type) 
                  || double.class.equals(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Double>() {
                @Override
                public Double get() {
                    final String value = props.getProperty(propertyName);
                    if (value == null)
                        return null;
                    return Double.valueOf(value);
                }
            };
        }
        else if ( Properties.class.isAssignableFrom(type)) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<Properties>() {
                @Override
                public Properties get() {
                    if (props.containsKey(propertyName)) {
                        throw new RuntimeException(propertyName + " is not a root for a properties structure");
                    }
                    String prefix = propertyName + ".";
                    Properties result = new Properties();
                    for (String prop : props.stringPropertyNames()) {
                        if (prop.startsWith(prefix)) {
                            result.setProperty(prop.substring(prefix.length()), props.getProperty(prop));
                        }
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

    @Override
    public ConfigurationSource getChild(String name) {
        String prefix = getPrefix() + name + ".";
        
        return new PropertiesConfigurationSource(
                null, 
                props.getProperty(prefix + "type"),     // TODO: Make 'type' configurable
                props, 
                prefix);
    }

    @Override
    public boolean isSimpleProperty(String propertyName) {
        if (null != props.getProperty(getPrefix() + propertyName)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return props.containsKey(Joiner.on(".").skipNulls().join(getPrefix(), propertyName));
    }

    @Override
    public Set<String> getUnknownProperties(Set<String> supportedProperties) {
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
