package com.netflix.fabricator.component;

import java.util.Collection;

import rx.Observable;

import com.netflix.fabricator.ConfigurationNode;
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
     * Find a existing component or return null if none exists
     * 
     * @param id
     * @return Existing component with matching id or null if none exists
     */
    public T find(String id);
    
    /**
     * Return true if the manager contains a component with the specified id
     * @param id
     * @return True if exists or false otherwise
     */
    public boolean contains(String id);
    
    /**
     * Get a component from a provided config that encapsulates a specific 
     * configuration such as an API requires containing JSON or property list 
     * payload.  Once created the managed entity will be registered using
     * the id provided in the config.  
     * 
     * @param config
     * @return Newly created component or cached component if already exists
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public T load(ConfigurationNode config) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    /**
     * Create an un-id'd component
     * 
     * @param config
     * @return Newly created component
     * @throws ComponentCreationException
     * @throws ComponentAlreadyExistsException 
     */
    public T create(ConfigurationNode config) throws ComponentCreationException, ComponentAlreadyExistsException;
    
    @Deprecated
    public T get(ConfigurationNode config) throws ComponentAlreadyExistsException, ComponentCreationException;

    /**
     * Add an externally created entity with the specified id.  Will throw
     * an exception if the id is already registered.
     * @param id
     * @param component
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public void add(String id, T component) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    /**
     * Add an externally created entity with the specified id.  Will replace
     * an existing component if one with the same id already exists
     * @param id
     * @param component
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public void replace(String id, T component) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    /**
     * Load a component and replace the component specified by config.getId()
     * 
     * @param config
     * @throws ComponentCreationException
     */
    public T replace(ConfigurationNode config) throws ComponentCreationException;
    
    /**
     * Apply the following function under a lock
     * @param run
     */
    public void apply(Runnable run);
    
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
