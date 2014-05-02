package com.netflix.fabricator;

public class ComponentType<T> {
    private final String type;
    
    public ComponentType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public static <T> ComponentType<T> from(String type) {
    	return new ComponentType<T> (type);
    }

}
