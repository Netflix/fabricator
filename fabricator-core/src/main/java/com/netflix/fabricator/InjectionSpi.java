package com.netflix.fabricator;

import java.lang.reflect.Method;

/**
 * Interface linking fabricator to a depdency injection framework.
 * 
 * @author elandau
 */
public interface InjectionSpi {
    /**
     * Create an instance of clazz and do any constructor, field or method injections
     * 
     * @param clazz
     * @return
     */
    public <T> T getInstance(Class<T> clazz);
    
    public PropertyBinder createInjectableProperty(
            String      propertyName,
            Class<?>    argType, 
            Method      method);
}
