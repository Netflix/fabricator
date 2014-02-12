package com.netflix.fabricator.component;

import java.util.Collection;
import java.util.List;

import rx.Observable;

import com.netflix.fabricator.ComponentConfiguration;
import com.netflix.fabricator.component.exception.ComponentAlreadyExistsException;
import com.netflix.fabricator.component.exception.ComponentCreationException;

/**
 * The ComponentManager is meant to be the entry point into a subsystem
 * that manages multiple instances of a component type, each having a 
 * unique ID.  These can be database connections or persistent connections
 * to an remote service.  Specific implementations of ComponentManager
 * will combine dependency injection with configuration mapping using
 * any of many different configuration specifications such as .properties
 * files, JSON blobs, key value pairs, YAML, etc.
 * 
 * @author elandau
 *
 * @param <T>
 * 
 * TODO: replace component
 * TODO: getIfExists
 */
public interface ComponentManager<T> {
    /**
     * Get a component by 'id'.  If the component does not exist one may be 
     * created from a previously specified configuration, such as a .properties
     * file.
     * 
     * @param id
     * @return
     * @throws ComponentCreationException 
     * @throws ComponentAlreadyExistsException 
     */
    public T get(String id) throws ComponentCreationException, ComponentAlreadyExistsException;
    
    /**
     * Get a component from a provided config that encapsulates a specific 
     * configuration such as an API requires containing JSON or property list 
     * payload.  Once created the managed entity will be registered using
     * the id provided in the config.  
     * 
     * @param config
     * @return
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public T load(ComponentConfiguration config) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    @Deprecated
    public T get(ComponentConfiguration config) throws ComponentAlreadyExistsException, ComponentCreationException;

    /**
     * Add an externally created entity with the specified id.  Will throw
     * an exception if the id is already registered.
     * @param id
     * @param component
     * @throws ComponentAlreadyExistsException 
     */
    public void add(String id, T component) throws ComponentAlreadyExistsException;
    
    /**
     * Add an externally created entity with the specified id.  Will replace
     * an existing component if one with the same id already exists
     * @param id
     * @param component
     * @throws ComponentAlreadyExistsException 
     */
    public void replace(String id, T component) throws ComponentAlreadyExistsException;
    
    /**
     * Replace all components with the components defined by this list of config
     * @param configs
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public void replaceAll(List<ComponentConfiguration> configs) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    /**
     * @return Return a collection of all component ids
     */
    public Collection<String> getIds();

    /**
     * Remove an element from the manager
     * @param id
     */
    public void remove(String id);
    
    /**
     * Visit all components
     */
    public Observable<T> asObservable();
}
