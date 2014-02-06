package com.netflix.fabricator.guice.mapping;

import com.netflix.fabricator.ComponentConfiguration;

/**
 * Created by hyuan on 1/16/14.
 */
public interface PropertyInjectionStrategy {
    PropertyInjectionStrategy addStrategy(BindingReslove concretePropertyInjectionImpl);
    boolean execute(String name, Object targetObj, ComponentConfiguration config) throws Exception;
}
