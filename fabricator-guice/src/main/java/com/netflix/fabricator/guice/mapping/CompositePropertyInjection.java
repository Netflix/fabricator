package com.netflix.fabricator.guice.mapping;

import com.google.inject.Injector;

import java.lang.reflect.Method;

/**
 * Created by hyuan on 1/16/14.
 */
public class CompositePropertyInjection extends SimplePropertyInjection {
    public CompositePropertyInjection(Class<?> argType, Injector injector, Method method) {
        super(argType, injector, method);
    }
}
