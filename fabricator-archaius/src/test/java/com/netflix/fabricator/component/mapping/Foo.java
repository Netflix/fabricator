package com.netflix.fabricator.component.mapping;

public interface Foo {
    public <T> String call(T entity) throws Exception;
}
