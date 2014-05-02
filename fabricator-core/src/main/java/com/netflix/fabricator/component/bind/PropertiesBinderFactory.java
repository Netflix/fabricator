package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;
import java.util.Properties;

import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;

public class PropertiesBinderFactory implements PropertyBinderFactory {
    private final static PropertiesBinderFactory instance = new PropertiesBinderFactory();
    
    public static PropertiesBinderFactory get() {
        return instance;
    }

    @Override
    public PropertyBinder createBinder(final Method method, final String propertyName) {
        final Class<?>[] types = method.getParameterTypes();
        final Class<?> clazz = types[0];
        if (!clazz.isAssignableFrom(Properties.class)) {
            return null;
        }
        
        return new PropertyBinder() {
            @Override
            public boolean bind(Object obj, ConfigurationNode node) throws Exception {
                ConfigurationNode child = node.getChild(propertyName);
                Properties props = child.getValue(Properties.class);
                if (props != null) {
                    method.invoke(obj, props);
                    return true;
                }
                else {
                    return false;
                }
            }
        };    
    }
}
