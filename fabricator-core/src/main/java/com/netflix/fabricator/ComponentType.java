package com.netflix.fabricator;

public class ComponentType<T> {
    private final String type;
    
    public ComponentType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }

}
