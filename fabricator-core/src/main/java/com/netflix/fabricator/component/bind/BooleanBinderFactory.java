package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;
import com.netflix.fabricator.ConfigurationSource;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;

public class BooleanBinderFactory implements PropertyBinderFactory {
    private final static BooleanBinderFactory instance = new BooleanBinderFactory();
    
    public static BooleanBinderFactory get() {
        return instance;
    }

    @Override
    public PropertyBinder createBinder(final Method method, final String propertyName) {
        final Class<?>[] types = method.getParameterTypes();
        final Class<?> clazz = types[0];
        if (!clazz.isAssignableFrom(Boolean.class) &&
            !clazz.equals(boolean.class)) {
            return null;
        }
        return new PropertyBinder() {
                @Override
                public boolean bind(Object obj, ConfigurationSource mapper) throws Exception {
                    Object value = mapper.getValue(propertyName, Boolean.class);
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
