package com.netflix.fabricator.properties;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.supplier.ListenableSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class PropertiesComponentConfiguration extends AbstractPropertiesComponentConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesComponentConfiguration.class);

    private final Properties props;
    
    public PropertiesComponentConfiguration(String id, String type, Properties props, String fullName) {
        super(id, type, fullName);
        this.props = props;
    }

    public PropertiesComponentConfiguration(String id, String type, Properties props) {
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
    public <T> ListenableSupplier<T> getDynamicValue(Class<T> type) {
        if ( String.class.isAssignableFrom(type) ) {
            return (ListenableSupplier<T>) new StaticListenableSupplier<String>() {
                @Override
                public String get() {
                    final String value = props.getProperty(getFullName());
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
                    final String value = props.getProperty(getFullName());
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
                    final String value = props.getProperty(getFullName());
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
                    final String value = props.getProperty(getFullName());
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
                    final String value = props.getProperty(getFullName());
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
                    if (props.containsKey(getFullName())) {
                        throw new RuntimeException(getFullName() + " is not a root for a properties structure");
                    }
                    String prefix = getFullName() + ".";
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
            LOG.warn(String.format("Unknown type '%s' for property '%s'", type.getCanonicalName(), getFullName()));
            return null;
        }
    }

    @Override
    public ConfigurationNode getChild(String name) {
        String fullName = Joiner.on(".").skipNulls().join(getFullName(), name);
        return new PropertiesComponentConfiguration(
                name, 
                props.getProperty(Joiner.on(".").join(fullName, "type")),     // TODO: Make 'type' configurable
                props, 
                fullName);
    }

    @Override
    public boolean isSingle() {
        return props.containsKey(getFullName());
    }

    @Override
    public boolean hasChild(String propertyName) {
        return props.containsKey(Joiner.on(".").skipNulls().join(getFullName(), propertyName));
    }

    @Override
    public Set<String> getUnknownProperties(Set<String> supportedProperties) {
        return Collections.emptySet();
    }

    @Override
    public <T> T getValue(Class<T> type) {
        Supplier<T> supplier = this.getDynamicValue(type);
        if (supplier != null)
            return supplier.get();
        return null;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
            .append("PropertiesComponentConfiguration[")
            .append("id=").append(getId())
            .append(",type=").append(getType())
            .append(",full=").append(getFullName())
            .append(",props=").append(props)
            .append("]")
            .toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((props == null) ? 0 : props.hashCode());
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
        PropertiesComponentConfiguration other = (PropertiesComponentConfiguration) obj;
        if (props == null) {
            if (other.props != null)
                return false;
        } else if (!props.equals(other.props))
            return false;
        return true;
    }
}
