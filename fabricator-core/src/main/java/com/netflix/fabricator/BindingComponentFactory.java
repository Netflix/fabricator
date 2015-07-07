package com.netflix.fabricator;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.netflix.fabricator.component.ComponentFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for creating a binding between a type string name and an
 * implementation using the builder pattern.
 * 
 * TODO: PostConstruct and PreDestroy
 * 
 * @author elandau
 *
 * @param <T>
 * 
 */
public class BindingComponentFactory<T>  {
    private static final Logger LOG = LoggerFactory.getLogger(BindingComponentFactory.class);
    
    private static final String BUILDER_METHOD_NAME = "builder";
    private static final String BUILD_METHOD_NAME   = "build";
    private static final String ID_METHOD_SUFFIX    = "Id"; // TODO: Make this configurable
    private static final String WITH_METHOD_PREFIX  = "with";
    private static final String SET_METHOD_PREFIX   = "set";
    
    private final ComponentFactory<T>        factory;
    
    public static interface Instantiator {
        public Object create(ConfigurationNode config) throws Exception ;
    }
    
    /**
     * Class determined to be the builder
     */
    private Class<?>                         builderClass;
    
    /**
     * Map of all object properties keyed by property name.  
     * 
     * TODO: What about multiple methods for the same property?
     */
    private final  Map<String, PropertyInfo> properties;
    
    /**
     * Implementation determined to be best method for instantiating an instance of T or it's builder
     */
    private Instantiator                     instantiator;
    
    private PropertyBinderResolver           binderResolver;
    
    public BindingComponentFactory(final Class<?> clazz, PropertyBinderResolver binderResolver, final InjectionSpi injector) {
        this.binderResolver = binderResolver;
        
        try {
            // Check if this class is a Builder<T> in which case just create
            // an instance of the builder and treat it as a builder with builder
            // method get().
            if (Builder.class.isAssignableFrom(clazz)) {
                this.builderClass = clazz;
                this.instantiator = new Instantiator() {
                    public Object create(@Nullable ConfigurationNode config) {
                        return (Builder<?>) injector.getInstance(clazz);
                    }
                };
            }
            // Check if there is a builder() method that returns an instance of the 
            // builder.
            else {
                final Method method = clazz.getMethod(BUILDER_METHOD_NAME);
                this.builderClass = method.getReturnType();
                this.instantiator = new Instantiator() {
                    public Object create(ConfigurationNode config) throws Exception {
                        Object obj = method.invoke(null, (Object[])null);
                        injector.injectMembers(obj);
                        return obj;
                    }
                };
            }
        } 
        catch (Exception e) {
            // Otherwise, look for a Builder inner class of T
            for (final Class<?> inner : clazz.getClasses()) {
                if (inner.getSimpleName().equals("Builder")) {
                    this.builderClass = inner;
                    this.instantiator = new Instantiator() {
                        public Object create(ConfigurationNode config) {
                            return injector.getInstance(inner);
                        }
                    };
                    break;
                }
            }
        }
        
        Preconditions.checkNotNull(builderClass, "No builder class found for " + clazz.getCanonicalName());
        
        properties = makePropertiesMap(builderClass);
        this.factory = new ComponentFactory<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T create(ConfigurationNode config) {
                try {
                    // 1. Create an instance of the builder.  This still will also do basic
                    //    dependency injection using @Inject.  Named injections will be handled
                    //    by the configuration mapping phase
                    Object builder = instantiator.create(config);

                    // 2. Set the 'id'
                    mapId(builder, config);

                    // 3. Apply configuration
                    mapConfiguration(builder, config);
                    
                    // 4. call build()
                    Method buildMethod = builder.getClass().getMethod(BUILD_METHOD_NAME);
                    return (T) buildMethod.invoke(builder);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Error creating component '%s' of type '%s'", config.getId(), clazz.getName()), e);
                }
            }

            @Override
            public Map<String, PropertyInfo> getProperties() {
                return properties;
            }

            @Override
            public Class<?> getRawType() {
                return clazz;
            }
        };
    }

    private void mapId(Object builder, ConfigurationNode config) throws Exception {
        if (config.getId() != null) {
            Method idMethod = null;
            try {
                idMethod = builder.getClass().getMethod(WITH_METHOD_PREFIX + ID_METHOD_SUFFIX, String.class);
            }catch (NoSuchMethodException e) {
                // OK to ignore
            }
            if(idMethod == null) {
                try {
                    idMethod = builder.getClass().getMethod(SET_METHOD_PREFIX + ID_METHOD_SUFFIX, String.class);
                }catch (NoSuchMethodException e) {
                    // OK to ignore
                }
            }
            if (idMethod != null) {
                idMethod.invoke(builder, config.getId());
            } else {
                LOG.trace("cannot find id method");
            }
        }
    }
    
    /**
     * Perform the actual configuration mapping by iterating through all parameters
     * and applying the config.
     * 
     * @param obj
     * @param config
     * @throws Exception
     */
    private void mapConfiguration(Object obj, ConfigurationNode node) throws Exception {
        for (Entry<String, PropertyInfo> prop : properties.entrySet()) {
            ConfigurationNode child = node.getChild(prop.getKey());
            if (child != null) {
                try {
                    prop.getValue().apply(obj, child);
                }
                catch (Exception e) {
                    throw new Exception("Failed to map property: " + prop.getKey(), e);
                }
            }
        }
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
    
    /**
     * Return all deduced properties.
     * 
     * @param builderClass
     * @return
     */
    private Map<String, PropertyInfo> makePropertiesMap(Class<?> builderClass) {
        Map<String, PropertyInfo> properties = Maps.newHashMap();
        
        // Iterate through each method and try to match a property
        for (final Method method : builderClass.getMethods()) {
            // Skip methods that do real DI.  These will have been injected at object creation
            if (hasInjectAnnotation(method)) {
                continue;
            }
            
            // Deduce property name from the method
            final String propertyName = getPropertyName(method);
            if (propertyName == null) {
                continue;
            }
            
            // Only support methods with a single parameter.
            // TODO: Might want to support methods that take TimeUnit
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 1) {
                continue;
            }
            
            PropertyInfo prop = new PropertyInfo(propertyName);
            PropertyBinder binding = binderResolver.get(method);
            if (binding != null) {
                prop.addBinding(binding);
                properties.put(propertyName, prop);
            }
        }
        
        return properties;
    }
    
    public ComponentFactory<T> get() {
        return factory;
    }
}
