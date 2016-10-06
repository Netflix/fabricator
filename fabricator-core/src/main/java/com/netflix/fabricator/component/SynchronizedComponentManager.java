package com.netflix.fabricator.component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.ComponentType;
import com.netflix.fabricator.ConfigurationNode;
import com.netflix.fabricator.TypeConfigurationResolver;
import com.netflix.fabricator.annotations.Default;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Implementation of a ComponentManager where each method is synchronized to guarantee
 * thread safety.  Uses guice MapBinder to specify the different types of component 
 * implementations.
 *  
 * @author elandau
 *
 * @param <T>
 */
public class SynchronizedComponentManager<T> implements ComponentManager<T> {
    private Logger LOG = LoggerFactory.getLogger(SynchronizedComponentManager.class);
    
    private final ConcurrentMap<String, T>         components = Maps.newConcurrentMap();
    private final Map<String, ComponentFactory<T>> factories;
    private final ComponentConfigurationResolver   configResolver;
    private final ComponentType<T>                 componentType;
    
    @Default
    @Inject(optional=true)
    private ComponentFactory<T> defaultComponentFactory = null;
    
    @Inject
    public SynchronizedComponentManager(
            ComponentType<T>                 type,
            Map<String, ComponentFactory<T>> factories, 
            TypeConfigurationResolver        config) {
        this.factories      = factories;
        this.componentType  = type;
        this.configResolver = config.getConfigurationFactory(type.getType());
    }
    
    @Override
    public synchronized T get(String id) throws ComponentCreationException, ComponentAlreadyExistsException {
        Preconditions.checkNotNull(id, String.format("Component of type '%s' must have a id", componentType.getType()));
        // Look for an existing component
        T component = components.get(id);
        if (component == null) {
            // Get configuration context from default configuration
            ConfigurationNode config = configResolver.getConfiguration(id);
            if (config != null) {
                // Create the object
                component = getComponentFactory(config.getType()).create(config);
                if (component == null) {
                    throw new ComponentCreationException(String.format("Error creating component of type '%s' with id '%s'", componentType.getType(), id));
                }

                addComponent(id, component);
            }
            else {
                throw new ComponentCreationException(String.format("No config provided for component of type '%s' with id '%s'", componentType.getType(), id));
            }
        }
        return component;
    }
    
    private void addComponent(String id, T component) throws ComponentCreationException{
        try {
            invokePostConstruct(component);
        } catch (Exception e) {
            throw new ComponentCreationException("Error creating component : " + id, e);
        }
                
        T oldComponent = components.put(id, component);
        if (oldComponent != null) {
            try {
                invokePreDestroy(oldComponent);
            } catch (Exception e) {
                LOG.error("Error destroying component : " + id, e);
            }
        }
    }

    private static void fillAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annot, Map<String, Method> methods) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isSynthetic() || method.isBridge()) {
                continue;
            }
    
            if (method.isAnnotationPresent(annot)) {
                methods.putIfAbsent(method.getName(), method);
            }
        }
    
        fillAnnotatedMethods(clazz.getSuperclass(), annot, methods);
        for (Class<?> face : clazz.getInterfaces()) {
            fillAnnotatedMethods(face, annot, methods);
        }
    }

    
    private void invokePostConstruct(T component) throws Exception {
        if (component == null)
            return;
        Map<String, Method> methods = new LinkedHashMap<>();
        fillAnnotatedMethods(component.getClass(), PostConstruct.class, methods);
        for (Method method : methods.values()) {
            method.invoke(component, null);
        }
    }

    private void invokePreDestroy(T component) throws Exception {
        if (component == null)
            return;
        Map<String, Method> methods = new LinkedHashMap<>();
        fillAnnotatedMethods(component.getClass(), PreDestroy.class, methods);
        for (Method method : methods.values()) {
            method.invoke(component, null);
        }
    }

    private void removeComponent(String id, T component) throws Exception {
        if (component == null)
            return;
        if (components.get(id) == component) {
            components.remove(id);
            invokePreDestroy(component);
        }
    }

    @Override
    public synchronized void add(String id, T component) throws ComponentAlreadyExistsException, ComponentCreationException {
        Preconditions.checkNotNull(id,        "Component must have a id");
        Preconditions.checkNotNull(component, "Component cannot be null");
        if (components.containsKey(id)) {
            throw new ComponentAlreadyExistsException(id);
        }
        addComponent(id, component);
    }

    @Override
    public synchronized Collection<String> getIds() {
        return ImmutableSet.copyOf(components.keySet());
    }
    
    @Override
    public synchronized T get(ConfigurationNode config) throws ComponentAlreadyExistsException, ComponentCreationException {
        return load(config);
    }

    
    @Override
    public synchronized T load(ConfigurationNode config) throws ComponentAlreadyExistsException, ComponentCreationException {
        Preconditions.checkNotNull(config,         "Configuration cannot be null");
        Preconditions.checkNotNull(config.getId(), "Configuration must have an id");
        
        if (config.getId() != null && components.containsKey(config.getId())) {
            throw new ComponentAlreadyExistsException(config.getId());
        }
        
        T component = getComponentFactory(config.getType()).create(config);
        if (component == null) {
            throw new ComponentCreationException(String.format("Error creating component type '%s' with id '%s'", componentType.getType(), config.getId()));
        }
        
        addComponent(config.getId(), component);
        
        return component;
    }

    @Override
    public T create(ConfigurationNode config) throws ComponentCreationException, ComponentAlreadyExistsException {
        T component = getComponentFactory(config.getType()).create(config);
        if (component == null) {
            throw new ComponentCreationException(String.format("Error creating component type '%s' with id '%s'", componentType.getType(), config.getId()));
        }
        
        try {
            invokePostConstruct(component);
        } catch (Exception e) {
            throw new ComponentCreationException("Error creating component : " + config.getId(), e);
        }

        return component;
    }

    @Override
    public synchronized void replace(String id, T component) throws ComponentAlreadyExistsException, ComponentCreationException {
        Preconditions.checkNotNull(id,       "Component must have a id");
        Preconditions.checkNotNull(component, "Component cannot be null");
        
        addComponent(id, component);
    }

    @Override
    public synchronized void remove(String id) {
        Preconditions.checkNotNull(id,       "Component must have a id");

        try {
            removeComponent(id, components.get(id));
        } catch (Exception e) {
            LOG.error("Error shutting down component: " + id, e);
        }
    }
    
    private ComponentFactory<T> getComponentFactory(String type) throws ComponentCreationException {
        ComponentFactory<T> factory = null;
        if (type != null) {
            factory = factories.get(type);
        }
        if (factory == null) {
            factory = defaultComponentFactory;
        }
        if (factory == null) {
            throw new ComponentCreationException(
                    String.format("Failed to create component '%s'. Invalid implementation specified '%s'.  Expecting one of '%s'.",
                                  this.componentType.getType(), type, factories.keySet()));
        }
        return factory;
    }

    @Override
    public synchronized void apply(Runnable operation) {
        operation.run();
    }

    @Override
    public synchronized T replace(ConfigurationNode config) throws ComponentCreationException {
        Preconditions.checkNotNull(config,         "Configuration cannot be null");
        Preconditions.checkNotNull(config.getId(), "Configuration must have an id");
        
        T component;
        try {
            component = getComponentFactory(config.getType()).create(config);
            if (component == null) {
                throw new ComponentCreationException(String.format("Error creating component type '%s' with id '%s'", componentType.getType(), config.getId()));
            }
            
            addComponent(config.getId(), component);
            return component;
        } catch (ComponentAlreadyExistsException e) {
            // This can't really happen
            throw new ComponentCreationException("Can't create component", e);
        }
    }

    @Override
    public synchronized T find(String id) {
        return components.get(id);
    }

    @Override
    public synchronized boolean contains(String id) {
        return components.containsKey(id);
    }
}
