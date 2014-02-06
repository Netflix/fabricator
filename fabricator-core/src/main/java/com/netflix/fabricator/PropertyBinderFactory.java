package com.netflix.fabricator;

import java.lang.reflect.Method;

/**
 * Abstraction for a factory to created bindings for specific types.  
 * There should be one of these for each argument type. Ex. String, Integer, ...
 * 
 * @author elandau
 *
 */
public interface PropertyBinderFactory {
    public PropertyBinder createBinder(Method method, String propertyName);
}
