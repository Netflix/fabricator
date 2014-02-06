package com.netflix.fabricator.guice.mapping;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.netflix.fabricator.ConfigurationSource;

import java.lang.reflect.Method;

/**
 * Created by hyuan on 1/16/14.
 */
public class NamedInjectionBinding implements BindingReslove {

    @Override
    public boolean execute(String name, Object obj, ConfigurationSource mapper, Class<?> argType, Injector injector, Method method) throws Exception {
        Binding<?> binding = injector.getExistingBinding(Key.get(argType, Names.named(name)));
        if (binding != null) {
            method.invoke(obj, binding.getProvider().get());
            return true;
        }
        return false;
    }
}
