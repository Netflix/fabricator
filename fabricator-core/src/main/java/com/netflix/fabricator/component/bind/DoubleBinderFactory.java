package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;

import com.netflix.fabricator.ConfigurationSource;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;

public class DoubleBinderFactory implements PropertyBinderFactory {
    private final static DoubleBinderFactory instance = new DoubleBinderFactory();
    
    public static DoubleBinderFactory get() {
        return instance;
    }

    @Override
    public PropertyBinder createBinder(final Method method, final String propertyName) {
        final Class<?>[] types = method.getParameterTypes();
        final Class<?> clazz = types[0];
        if (!clazz.isAssignableFrom(Double.class) &&
            !clazz.equals(double.class)) {
            return null;
        }
        return new PropertyBinder() {
                @Override
                public boolean bind(Object obj, ConfigurationSource mapper) throws Exception {
                    Object value = mapper.getValue(propertyName, Double.class);
                    if (value != null) {
                        method.invoke(obj, value);
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };    
    }
}
