package com.netflix.fabricator.component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.netflix.fabricator.ComponentType;
import com.netflix.fabricator.ComponentConfigurationResolver;
import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.TypeConfigurationResolver;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;

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
    
    private static final String DEFAULT_FACTORY = "default";
   
    private final ConcurrentMap<String, T>         components = Maps.newConcurrentMap();
    private final Map<String, ComponentFactory<T>> factories;
    private final ComponentConfigurationResolver             config;
    
    @Inject
    public SynchronizedComponentManager(
            ComponentType<T>                 type,
            Map<String, ComponentFactory<T>> factories, 
            TypeConfigurationResolver         config) {
        this.factories = factories;
        this.config = config.getConfigurationFactory(type.getType());
    }
    
    @Override
    public T get(String key) throws ComponentCreationException, ComponentAlreadyExistsException {
        Preconditions.checkNotNull(key, "Component must have a key");
        T component = components.get(key);
        if (component == null) {
            synchronized (this) {
                component = components.get(key);
                if (component == null) {
                    ComponentConfiguration mapper = config.getConfiguration(key);
                    if (mapper != null) {
                        component = getComponentFactory(mapper.getType()).create(mapper);
                        if (component == null) {
                            throw new ComponentCreationException("Error creating component");
                        }
        
                        components.put(key, component);
                    }
                    else {
                        throw new ComponentCreationException("No mapper provided");
                    }
                }
            }
        }
        return component;
    }

    @Override
    public synchronized void add(String key, T component) throws ComponentAlreadyExistsException {
        Preconditions.checkNotNull(key,       "Component must have a key");
        Preconditions.checkNotNull(component, "Component cannot be null");
        if (null != components.putIfAbsent(key, component)) {
            throw new ComponentAlreadyExistsException(key);
        }
    }

    @Override
    public synchronized Collection<String> keys() {
        return components.keySet();
    }
    
    @Override
    public synchronized T get(ComponentConfiguration mapper) throws ComponentAlreadyExistsException, ComponentCreationException {
        Preconditions.checkNotNull(mapper,          "Mapper cannot be null");
        Preconditions.checkNotNull(mapper.getKey(), "Mapper must have key");
        
        if (mapper.getKey() != null && components.containsKey(mapper.getKey())) {
            throw new ComponentAlreadyExistsException(mapper.getKey());
        }
        
        T component = getComponentFactory(mapper.getType()).create(mapper);
        if (component == null) {
            throw new ComponentCreationException(String.format("Error creating component '%s'", mapper.getKey()));
        }
        
        components.put(mapper.getKey(), component);
        
        return component;
    }

    @Override
    public synchronized void replaceAll(List<ComponentConfiguration> mappers) throws ComponentAlreadyExistsException, ComponentCreationException {
//        Preconditions.checkNotNull(mappers, "Mapper cannot be null");
//        
//        Map<String, Holder> newComponents = Maps.newHashMap();
//        
//        // Create an internal list of mappers
//        for (ConfigurationSource mapper : mappers) {
//            T component = factories.get(mapper.getType()).create(mapper);
//            if (component != null && mapper.getKey() != null) {
//                if (newComponents.containsKey(mapper.getKey())) {
//                    throw new ComponentAlreadyExistsException(String.format("Duplicate key for mapper '%s'", mapper.getKey()));
//                }
//                newComponents.put(mapper.getKey(), new Holder(component));
//            }
//        }
//        
//        MapDifference<String, Holder> diff = Maps.difference(newComponents, components);
//        // New entries
//        for (Entry<String, Holder> entry : diff.entriesOnlyOnLeft().entrySet()) {
//            components.put(entry.getKey(), entry.getValue());
//        }
//        
//        // Entries that were removed
//        for (Entry<String, Holder> entry : diff.entriesOnlyOnRight().entrySet()) {
//            components.remove(entry.getKey());
//            try {
//                entry.getValue().set(null);
//            }
//            catch (Exception e) {
//                LOG.error("Failed to remove component " + entry.getKey(), e);
//            }
//        }
//        
//        // Entries that changed
//        for (Entry<String, Holder> entry : diff.entriesInCommon().entrySet()) {
//            // TODO:
//        }
    }

    @Override
    public synchronized void replace(String key, T component) throws ComponentAlreadyExistsException {
        Preconditions.checkNotNull(key,       "Component must have a key");
        Preconditions.checkNotNull(component, "Component cannot be null");
        components.put(key, component);
    }

    @Override
    public synchronized void remove(String key) {
        Preconditions.checkNotNull(key,       "Component must have a key");

        components.remove(key);
    }
    
    private ComponentFactory<T> getComponentFactory(String type) throws ComponentCreationException {
        ComponentFactory<T> factory = null;
        if (type != null) {
            factory = factories.get(type);
        }
        if (factory == null) {
            factory = factories.get(DEFAULT_FACTORY);
        }
        if (factory == null) {
            throw new ComponentCreationException(
                    String.format("Failed to create component. Invalid type '%s'.  Expecting one of '%s'.",
                                  type, factories.keySet()));
        }
        return factory;
    }

    @Override
    public Observable<T> asObservable() {
        return Observable.from(components.values());
    }

}
