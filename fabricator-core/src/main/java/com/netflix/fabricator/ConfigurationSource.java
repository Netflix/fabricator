package com.netflix.fabricator;

import java.util.Set;

import com.netflix.fabricator.supplier.ListenableSupplier;

/**
 * The mapper provides a configuration bridge between an instance of a type
 * and a configuration.  The mapper works with the ComponentManager in that it
 * provides additional information such as the key and type specified in the 
 * configuration so that the manager may create the appropriate type and then
 * call the mapper to apply the configuration to the created object.
 * 
 * There will be one implementation of Mapper for each configuration type such
 * as .properties file or JSON payload from a request.
 * 
 * @author elandau
 */
public interface ConfigurationSource {
    /**
     * Provide the unique key for the component specified in the configuration.
     * Can be null for components that are not to be cached or when accessing
     * a subconfiguration such as a policy.
     * 
     * @return
     */
    public String getKey();
    
    /**
     * Get the component type.  When using MapBinder to provide different implementations
     * the type will match the key in the MapBinder.  
     * @return
     */
    public String getType();
    
    /**
     * Get a value for the property
     * 
     * @param propertyName  Simple property name (no prefix)
     * @param type          Expected value type (ex. Integer.class)
     * @return The value or null if not found or type not supported
     */
    public <T> T getValue(String propertyName, Class<T> type);
    
    /**
     * Get a dynamic version of the property with optional on change notification
     * 
     * @param propertyName  Simple property name (no prefix)
     * @param type          Expected value type (ex. Integer.class)
     * @return A supplier to the value or null if type not supported
     */
    public <T> ListenableSupplier<T> getDynamicValue(String propertyName, Class<T> type);

    /**
     * @param propertyName
     * @return Return true if the property is a single value.  Return false if the property
     *          is a nested structure
     */
    public boolean isSimpleProperty(String propertyName);
    
    /**
     * @param propertyName
     * @return Return true if the property exists.
     */
    public boolean hasProperty(String propertyName);
    
    /**
     * Return a list of properties in the configuration for which there is no method.
     * 
     * @param supportedProperties
     * @return Return a set of unknown properties.
     */
    public Set<String> getUnknownProperties(Set<String> supportedProperties);
    
    /**
     * Return a Mapper that a sub-context of the underlying configuration.  
     * For example,
     * 
     * getSubMapper("policy") on the following JSON will return the node rooted at "policy"
     * 
     *      {
     *          "prop1" : "abc",
     *          "policy" : {
     *              "prop2" : "def"
     *          },
     *      }
     *      
     *
     * @return
     */
    public ConfigurationSource getChild(String propertyName);
}
