package com.netflix.fabricator.guice.mapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.netflix.fabricator.ComponentConfiguration;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by hyuan on 1/16/14.
 */
public class SimplePropertyInjection implements PropertyInjectionStrategy {
    private final List<BindingReslove> injectionStrategies;
    private final Class<?> argType;
    private final Injector injector;
    private final Method buildMethod;


    public SimplePropertyInjection(Class<?> argType, Injector injector, Method method) {
        Preconditions.checkNotNull(argType);
        Preconditions.checkNotNull(injector);
        Preconditions.checkNotNull(method);
        this.argType = argType;
        this.injector = injector;
        this.buildMethod = method;
        injectionStrategies = Lists.newArrayList();
    }

    @Override
    public PropertyInjectionStrategy addStrategy(BindingReslove concretePropertyInjectionImpl) {
        injectionStrategies.add(concretePropertyInjectionImpl);
        return this;
    }

    @Override
    public boolean execute(String name, Object targetObj, ComponentConfiguration config) throws Exception {
        for (BindingReslove eachInjectionStrategy : injectionStrategies) {
            if (eachInjectionStrategy.execute(name, targetObj, config, argType, injector, buildMethod)) {
                return true;
            }
        }
        return false;
    }
}
