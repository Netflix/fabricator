package com.netflix.fabricator.component;

import java.util.Collection;
import java.util.List;

import rx.Observable;

import com.netflix.fabricator.ConfigurationSource;
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
     * Get a component by 'key'.  If the component does not exist one may be 
     * created from a previously specified configuration, such as a .properties
     * file.
     * 
     * @param key
     * @return
     * @throws ComponentCreationException 
     * @throws ComponentAlreadyExistsException 
     */
    public T get(String key) throws ComponentCreationException, ComponentAlreadyExistsException;
    
    /**
     * Get a component from a provided Mapper that encapsulates a specific 
     * configuration such as an API requires containing JSON or property list 
     * payload.  Once created the managed entity will be registered using
     * the key provided in the mapper.  
     * 
     * @param mapper
     * @return
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public T get(ConfigurationSource mapper) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    /**
     * Add an externally created entity with the specified key.  Will throw
     * an exception if the key is already registered.
     * @param key
     * @param component
     * @throws ComponentAlreadyExistsException 
     */
    public void add(String key, T component) throws ComponentAlreadyExistsException;
    
    /**
     * Add an externally created entity with the specified key.  Will replace
     * an existing component if one with the same key already exists
     * @param key
     * @param component
     * @throws ComponentAlreadyExistsException 
     */
    public void replace(String key, T component) throws ComponentAlreadyExistsException;
    
    /**
     * Replace all components with the components defined by this list of mappers
     * @param mappers
     * @throws ComponentAlreadyExistsException 
     * @throws ComponentCreationException 
     */
    public void replaceAll(List<ConfigurationSource> mappers) throws ComponentAlreadyExistsException, ComponentCreationException;
    
    /**
     * @return Return a collection of all component keys
     */
    public Collection<String> keys();

    /**
     * Remove an element from the manager
     * @param key
     */
    public void remove(String key);
    
    /**
     * Visit all components
     */
    public Observable<T> asObservable();
}
