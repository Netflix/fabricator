package com.netflix.fabricator.guice.mapping;

import com.google.inject.Injector;
import com.netflix.fabricator.ConfigurationNode;

import java.lang.reflect.Method;

/**
 * Created by hyuan on 1/16/14.
 */
public interface BindingReslove {
    boolean execute(String name, Object obj, ConfigurationNode config, Class<?> argType, Injector injector, Method method) throws Exception;
}
