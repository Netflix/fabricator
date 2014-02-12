package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;

import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;

public class ClassBinderFactory implements PropertyBinderFactory {
    private final static ClassBinderFactory instance = new ClassBinderFactory();
    
    public static ClassBinderFactory get() {
        return instance;
    }

    @Override
    public PropertyBinder createBinder(final Method method, final String propertyName) {
        final Class<?>[] types = method.getParameterTypes();
        final Class<?> clazz = types[0];
        
        if (!clazz.equals(Class.class))
            return null; 
        
        return new PropertyBinder() {
                @Override
                public boolean bind(Object obj, ComponentConfiguration config) throws Exception {
                    String value = config.getValue(propertyName, String.class);
                    if (value != null) {
                        method.invoke(obj, Class.forName(value));
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };    
    }
}