package com.netflix.fabricator.component.bind;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.netflix.fabricator.InjectionSpi;
import com.netflix.fabricator.PropertyBinder;
import com.netflix.fabricator.PropertyBinderFactory;
import com.netflix.fabricator.PropertyBinderResolver;

public class SimplePropertyBinderFactoryResolver implements PropertyBinderResolver {
    private static final Logger LOG = LoggerFactory.getLogger(SimplePropertyBinderFactoryResolver.class);
    
    private static final String WITH_METHOD_PREFIX  = "with";
    private static final String SET_METHOD_PREFIX   = "set";
    private static final List<PropertyBinderFactory> DEFAULT_PROPERTY_FACTORIES = Lists.newArrayList( 
            StringBinderFactory.get(),
            LongBinderFactory.get(),
            DoubleBinderFactory.get(),
            BooleanBinderFactory.get(),
            IntegerBinderFactory.get(),
            EnumBinderFactory.get(),
            ClassBinderFactory.get(),
            DynamicStringBinderFactory.get(),
            DynamicLongBinderFactory.get(),
            DynamicDoubleBinderFactory.get(),
            DynamicBooleanBinderFactory.get(),
            DynamicIntegerBinderFactory.get(),
            PropertiesBinderFactory.get()
            );
    
    private final List<PropertyBinderFactory> propertyBinders;
    private final InjectionSpi injector;
    
    public SimplePropertyBinderFactoryResolver(List<PropertyBinderFactory> propertyBinders, InjectionSpi injector) {
        if (propertyBinders != null)
            this.propertyBinders = Lists.newArrayList(propertyBinders);
        else
            this.propertyBinders = Lists.newArrayList();
        this.propertyBinders.addAll(DEFAULT_PROPERTY_FACTORIES);
        this.injector = injector;
    }
    
    public SimplePropertyBinderFactoryResolver() {
    	this(null, null);
    }
    
    @Override
    public PropertyBinder get(Method method) {
        // Skip methods that do real DI.  These will have been injected at object creation
        if (hasInjectAnnotation(method)) {
            return null;
        }
        
        // Deduce property name from the method
        final String propertyName = getPropertyName(method);
        if (propertyName == null) {
            return null;
        }
        
        // Only support methods with a single parameter.
        // TODO: Might want to support methods that take TimeUnit
        Class<?>[] types = method.getParameterTypes();
        if (types.length != 1) {
            return null;
        }
        
        // Primitive or String will be handled as configuration binding
        final Class<?> argType = types[0];
        
        for (PropertyBinderFactory factory : propertyBinders) {
            PropertyBinder binder = factory.createBinder(method, propertyName);
            if (binder != null) {
                return binder;
            }
        }
        
        return injector.createInjectableProperty(propertyName, argType, method);
    }

    private static boolean hasInjectAnnotation(Method method) {
        return method.isAnnotationPresent(Inject.class) ||
                method.isAnnotationPresent(javax.inject.Inject.class);
    }
    
    private static String getPropertyName(Method method) {
        if (method.getName().startsWith(WITH_METHOD_PREFIX)) {
            return CaseFormat.UPPER_CAMEL.to(
                    CaseFormat.LOWER_CAMEL, 
                        StringUtils.substringAfter(method.getName(), WITH_METHOD_PREFIX));
        }
        
        if (method.getName().startsWith(SET_METHOD_PREFIX)) {
            return CaseFormat.UPPER_CAMEL.to(
                    CaseFormat.LOWER_CAMEL, 
                        StringUtils.substringAfter(method.getName(), SET_METHOD_PREFIX));
        }
        
        return null;
    }
}
