package com.netflix.fabricator.component.mapping;

public class FooImpl implements Foo {
    private String constant;
    
    public FooImpl(String constant) {
        this.constant = constant;
    }
    
    @Override
    public <T> String call(T entity) throws Exception {
        return constant + entity;
    }
}
