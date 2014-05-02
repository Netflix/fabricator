package com.netflix.fabricator.properties;

import com.netflix.fabricator.ConfigurationNode;

/**
 * Base source for 'properties' driven configuration where each 'child' 
 * is namespaced by a property prefix
 * 
 * @author elandau
 *
 */
public abstract class AbstractPropertiesComponentConfiguration implements ConfigurationNode {
    /**
     * This is the property/field name
     */
    private final String     id;
    
    /**
     * Element type stored in this property.
     * 
     * TODO: may considering moving this out
     */
    private final String     type;
    
    /**
     * This is the full property name with prefix
     */
    private final String     fullName;
    
    public AbstractPropertiesComponentConfiguration(String id, String type) {
        this.id    = id;
        this.type  = type;
        this.fullName = "";
    }
    
    public AbstractPropertiesComponentConfiguration(String id, String type, String fullName) {
        this.id     = id;
        this.type   = type;
        this.fullName = fullName;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }
    
    public String getFullName() {
        return this.fullName;
    }
}
