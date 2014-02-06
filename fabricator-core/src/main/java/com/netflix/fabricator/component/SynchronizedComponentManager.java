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
    private final ComponentConfigurationResolver   configResolver;
    
    @Inject
    public SynchronizedComponentManager(
            ComponentType<T>                 type,
            Map<String, ComponentFactory<T>> factories, 
            TypeConfigurationResolver         config) {
        this.factories      = factories;
        this.configResolver = config.getConfigurationFactory(type.getType());
    }
    
    @Override
    public T get(String id) throws ComponentCreationException, ComponentAlreadyExistsException {
        Preconditions.checkNotNull(id, "Component must have a id");
        T component = components.get(id);
        if (component == null) {
            synchronized (this) {
                component = components.get(id);
                if (component == null) {
                    ComponentConfiguration config = configResolver.getConfiguration(id);
                    if (config != null) {
                        component = getComponentFactory(config.getType()).create(config);
                        if (component == null) {
                            throw new ComponentCreationException("Error creating component");
                        }
        
                        components.put(id, component);
                    }
                    else {
                        throw new ComponentCreationException("No config provided");
                    }
                }
            }
        }
        return component;
    }

    @Override
    public synchronized void add(String id, T component) throws ComponentAlreadyExistsException {
        Preconditions.checkNotNull(id,        "Component must have a id");
        Preconditions.checkNotNull(component, "Component cannot be null");
        if (null != components.putIfAbsent(id, component)) {
            throw new ComponentAlreadyExistsException(id);
        }
    }

    @Override
    public synchronized Collection<String> getIds() {
        return components.keySet();
    }
    
    @Override
    public synchronized T get(ComponentConfiguration config) throws ComponentAlreadyExistsException, ComponentCreationException {
        Preconditions.checkNotNull(config,         "Configuration cannot be null");
        Preconditions.checkNotNull(config.getId(), "Configuration must have an id");
        
        if (config.getId() != null && components.containsKey(config.getId())) {
            throw new ComponentAlreadyExistsException(config.getId());
        }
        
        T component = getComponentFactory(config.getType()).create(config);
        if (component == null) {
            throw new ComponentCreationException(String.format("Error creating component '%s'", config.getId()));
        }
        
        components.put(config.getId(), component);
        
        return component;
    }

    @Override
    public synchronized void replaceAll(List<ComponentConfiguration> configs) throws ComponentAlreadyExistsException, ComponentCreationException {
//        Preconditions.checkNotNull(configs, "Config cannot be null");
//        
//        Map<String, Holder> newComponents = Maps.newHashMap();
//        
//        // Create an internal list of configs
//        for (ConfigurationSource config : configs) {
//            T component = factories.get(config.getType()).create(config);
//            if (component != null && config.getId() != null) {
//                if (newComponents.containsKey(config.getId())) {
//                    throw new ComponentAlreadyExistsException(String.format("Duplicate id for config '%s'", config.getId()));
//                }
//                newComponents.put(config.getId(), new Holder(component));
//            }
//        }
//        
//        MapDifference<String, Holder> diff = Maps.difference(newComponents, components);
//        // New entries
//        for (Entry<String, Holder> entry : diff.entriesOnlyOnLeft().entrySet()) {
//            components.put(entry.getId(), entry.getValue());
//        }
//        
//        // Entries that were removed
//        for (Entry<String, Holder> entry : diff.entriesOnlyOnRight().entrySet()) {
//            components.remove(entry.getId());
//            try {
//                entry.getValue().set(null);
//            }
//            catch (Exception e) {
//                LOG.error("Failed to remove component " + entry.getId(), e);
//            }
//        }
//        
//        // Entries that changed
//        for (Entry<String, Holder> entry : diff.entriesInCommon().entrySet()) {
//            // TODO:
//        }
    }

    @Override
    public synchronized void replace(String id, T component) throws ComponentAlreadyExistsException {
        Preconditions.checkNotNull(id,       "Component must have a id");
        Preconditions.checkNotNull(component, "Component cannot be null");
        components.put(id, component);
    }

    @Override
    public synchronized void remove(String id) {
        Preconditions.checkNotNull(id,       "Component must have a id");

        components.remove(id);
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
