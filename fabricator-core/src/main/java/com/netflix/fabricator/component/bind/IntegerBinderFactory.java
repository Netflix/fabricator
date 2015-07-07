package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;

import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;

public class IntegerBinderFactory implements PropertyBinderFactory {

    private final static IntegerBinderFactory instance = new IntegerBinderFactory();
    
    public static IntegerBinderFactory get() {
        return instance;
    }

    @Override
    public PropertyBinder createBinder(final Method method, final String propertyName) {
        final Class<?>[] types = method.getParameterTypes();
        final Class<?> clazz = types[0];
        
        if (!clazz.isAssignableFrom(Integer.class) &&
            !clazz.equals(int.class)) {
            return null;
        }
        
        return new PropertyBinder() {
            @Override
            public boolean bind(Object obj, ConfigurationNode node) throws Exception {
                Object value = node.getValue(Integer.class);
                if (value != null) {
                    method.invoke(obj, value);
                    return true;
                }
                else {
                    return false;
                }
            }
            
            public String toString() {
                return "IntegerBinderFactory["+ propertyName + "]";
            }
        };    
    }
}
